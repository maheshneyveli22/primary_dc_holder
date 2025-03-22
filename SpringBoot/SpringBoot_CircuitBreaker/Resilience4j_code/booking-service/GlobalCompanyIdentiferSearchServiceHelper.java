package com.expd.expo.booking.core.reference.finder;

import javax.annotation.Resource;

import com.expd.service.global_company_identifier.contract.v1.GetOrganizationSummaryRequest;
import com.expd.service.global_company_identifier.contract.v1.GetOrganizationSummaryResponse;
import com.expd.service.global_company_identifier.dto.v1.OrgSummary;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.expd.arch.core.response.ResponseMessage;
import com.expd.service.global_company_identifier.contract.v1.GetOrganizationRequest;
import com.expd.service.global_company_identifier.contract.v1.GetOrganizationResponse;
import com.expd.service.global_company_identifier.dto.v1.Branch;
import com.expd.service.global_company_identifier.dto.v1.OrgRole;
import com.expd.service.global_company_identifier.dto.v1.Organization;
import com.expd.service.global_company_identifier.v1.IGlobalCompanyIdentifierService;
import com.expd.expo.booking.core.serviceSoapTemplate.GlobalCompanyIdentifierServiceException;

import java.util.ArrayList;
import java.util.List;

@Component
public class GlobalCompanyIdentiferSearchServiceHelper implements GlobalCompanyIdentiferFinder {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private String GCI_ORG_ROLE_TYPE = "CUST";
    private String GCI_ENABLED_STATUS = "ENAB";
    private String EXCEPTION_MSG = "The GCI service threw the following exception: ";

    @Resource(name = "globalCompanyIdentifierTemplate")
    private IGlobalCompanyIdentifierService gciSearchService;

    /*
     * Call the GCI service to find multiple active customers whose gci code or
     * name matches the search string.
     * 
     * @param String searchValue - the gciCode or name of the active customer
     * 
     * @return a GlobalCompanyIdentifierForm whose data is populated with the
     * found active customers
     */
    @Override
    public GlobalCompaniesIdentifierForm findCustomers(String searchValue)
            throws GlobalCompanyIdentifierServiceException {
	GlobalCompaniesIdentifierForm multipleRespForm = new GlobalCompaniesIdentifierForm();
	if (this.isGCICodeSearch(searchValue)) {
	    // search for the customer by GCI code
	    GlobalCompanyIdentifierForm singleRespForm = findCustomerWithGCICode(searchValue);
	    if (singleRespForm != null) {
		multipleRespForm.setTotalFound(1);
		multipleRespForm.getForms().add(singleRespForm);
	    }
	} else {
	    // build the request
	    GetOrganizationRequest request = new GetOrganizationRequest();
	    request.setCompanyName(searchValue);
	    // call the service
	    GetOrganizationResponse response = accessGCISearchService(request);
	    // handle the response
	    if (hasRespErrors(response)) {
		handleErrors(response);
	    } else {
		populateGlobalCompaniesIdentifierForm(multipleRespForm, response);
	    }
	}
	return multipleRespForm;
    }

    private void populateGlobalCompaniesIdentifierForm(GlobalCompaniesIdentifierForm form,
            GetOrganizationResponse response) {
	form.setTotalFound(response.getActualNumberOfResults());
	for (Organization org : response.getResults()) {
	    if (org.isPublished()) {
		form.getForms().add(createGlobalCompanyIdentifierForm(org));
	    }
	}
    }

    private boolean isGCICodeSearch(String searchValue) {
	if (StringUtils.isBlank(searchValue) || searchValue.length() < 3) {
	    return false;
	}
	String searchValueUpper = searchValue.toUpperCase();
	if (!searchValueUpper.startsWith("G")) {
	    return false;
	}
	String remainingChars = searchValueUpper.substring(1);
	try {
	    Integer.parseInt(remainingChars);
	} catch (NumberFormatException e) {
	    return false;
	}
	return true;
    }

    private GetOrganizationResponse accessGCISearchService(GetOrganizationRequest request)
            throws GlobalCompanyIdentifierServiceException {
	// set the request to only return active customer gcis
	request.setOrgRoleStatusCode(GCI_ENABLED_STATUS);
	request.setOrgRoleTypeCode(GCI_ORG_ROLE_TYPE);
	// call the service
	GetOrganizationResponse response = null;
	try {
	    response = gciSearchService.getOrganizations(request);
	} catch (RuntimeException e) {
	    LOG.error(EXCEPTION_MSG + e.getMessage());
	    throw new GlobalCompanyIdentifierServiceException(EXCEPTION_MSG, e);
	}
	return response;
    }

    /*
     * Call the GCI service to find active customers whose gci code matches the
     * gciCode string.
     * 
     * @param String gciCode - the gciCode of the active customer
     * 
     * @return a GlobalCompanyIdentifierForm whose data is populated with the
     * found active customer
     */
    @Override
    @Cacheable(value = "customerGCICodeCache")
    public GlobalCompanyIdentifierForm findCustomerWithGCICode(String organizationGCICode)
            throws GlobalCompanyIdentifierServiceException {
	Organization org = findSinglePublishedResultFromGCIService(StringUtils.upperCase(organizationGCICode));
	if (org == null) {
	    return null;
	} else {
	    GlobalCompanyIdentifierForm gciForm = createGlobalCompanyIdentifierForm(org);
	    return gciForm;
	}
    }

    @Override
    @CacheEvict(value = "customerGCICodeCache", allEntries = true)
    public void clearCustomerGCICache() {
	LOG.info("Customer GCI cache is cleared");
    }

	@Override
	@CircuitBreaker(name = "findgci",fallbackMethod = "fallbackGCI")
	public OrgSummary findCustomerOrganizationSummaryWithGCICode(String organizationGCICode) throws GlobalCompanyIdentifierServiceException {
		OrgSummary org = getOrganizationSummary(StringUtils.upperCase(organizationGCICode));
		if (org == null) {
			return null;
		} else {
			return org;
		}
	}

	 public OrgSummary fallbackGCI(String organizationGCICode,Throwable exception){
		 System.out.println("Mahesh in helper class When GCI failed");
    return null;
	}

	private OrgSummary getOrganizationSummary(String gciCode)
			throws GlobalCompanyIdentifierServiceException {
		// build the request
		GetOrganizationSummaryRequest request = new GetOrganizationSummaryRequest();
		List<String> gciNumbers = new ArrayList<>();
		gciNumbers.add(gciCode);
		request.setGciNumbers(gciNumbers);
		// call the service
		GetOrganizationSummaryResponse response = accessGCISummaryService(request);
		// handle the response
		OrgSummary org = null;
		if (hasRespErrors(response)) {
			handleErrors(response);
		} else {
			if (response.getResult().size() == 1) {
				org = response.getResult().get(0);
			}
		}
		return org;
	}

	private GetOrganizationSummaryResponse accessGCISummaryService(GetOrganizationSummaryRequest request)
			throws GlobalCompanyIdentifierServiceException {

		GetOrganizationSummaryResponse response = null;
		try {
			response = gciSearchService.getOrganizationSummary(request);
		} catch (RuntimeException e) {
			LOG.error(EXCEPTION_MSG + e.getMessage());
			throw new GlobalCompanyIdentifierServiceException(EXCEPTION_MSG, e);
		}
		return response;
	}

	private boolean hasRespErrors(GetOrganizationSummaryResponse response) {
		return response == null || !response.getMessages().isEmpty();
	}

	private void handleErrors(GetOrganizationSummaryResponse response) {
		LOG.error("The GCI service response contains errors.");
		for (ResponseMessage msg : response.getMessages()) {
			LOG.error(createLogMessage(msg));
		}
	}

	private Organization findSinglePublishedResultFromGCIService(String gciCode)
            throws GlobalCompanyIdentifierServiceException {
	// build the request
	GetOrganizationRequest request = new GetOrganizationRequest();
	request.setCompanyId(gciCode);
	// call the service
	GetOrganizationResponse response = accessGCISearchService(request);
	// handle the response
	Organization org = null;
	if (hasRespErrors(response)) {
	    handleErrors(response);
	} else {
	    if (response.getResults().size() == 1) {
		Organization foundOrg = response.getResults().get(0);
		if (foundOrg.isPublished() && isCustomerOrganization(foundOrg)) {
		    org = foundOrg;
		}
	    }
	}
	return org;
    }

    private boolean isCustomerOrganization(Organization foundOrg) {
	for (OrgRole role : foundOrg.getOrgRoles()) {
	    if (GCI_ORG_ROLE_TYPE.equalsIgnoreCase(role.getTypeCode())
	            && GCI_ENABLED_STATUS.equalsIgnoreCase(role.getStatusCode())) {
		return true;
	    }
	}
	return false;

    }

    private GlobalCompanyIdentifierForm createGlobalCompanyIdentifierForm(Organization org) {
	GlobalCompanyIdentifierForm form = new GlobalCompanyIdentifierForm();
	form.setOrganizationGCICode(org.getGciNumber());
	form.setOrganizationName(org.getLegalName());
	form.setGlobalBranchAffiliation(org.isGlobalBranchAffiliation());
	for (Branch branch : org.getAssociatedBranches()) {
	    form.getAssociatedBranches().add(branch.getCode());
	}
	return form;
    }

    /*
     * utility error processing
     */

    private boolean hasRespErrors(GetOrganizationResponse response) {
	return response == null || !response.getMessages().isEmpty();
    }

    private void handleErrors(GetOrganizationResponse response) {
	LOG.error("The GCI service response contains errors.");
	for (ResponseMessage msg : response.getMessages()) {
	    LOG.error(createLogMessage(msg));
	}
    }

    private String createLogMessage(ResponseMessage message) {
	return "ResponseMessage: " + "Severity: " + message.getSeverity() + ". Number:" + message.getNumber()
	        + ". Text: " + message.getText() + ". FieldNames:" + message.getFieldNames() + ". ReplacementStrings:"
	        + message.getReplacementStrings();
    }

}
