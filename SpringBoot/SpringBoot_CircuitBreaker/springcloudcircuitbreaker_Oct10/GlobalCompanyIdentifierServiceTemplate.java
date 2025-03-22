package com.expd.expo.booking.core.serviceSoapTemplate;


import com.expd.expo.booking.util.JaxbContextUtil;
import com.expd.service.global_company_identifier.v1.GetOrganizationSummary;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.SecurityHeaderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;

import com.expd.service.global_company_identifier.contract.v1.*;
import com.expd.service.global_company_identifier.v1.GetOrganizations;
import com.expd.service.global_company_identifier.v1.IGlobalCompanyIdentifierService;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component("globalCompanyIdentifierTemplate")
public class GlobalCompanyIdentifierServiceTemplate implements IGlobalCompanyIdentifierService {

    private WssSecurityUtil jaxbUtil;

    private WebServiceTemplate webServiceTemplate;

    private String globalCompanyIdentifierServiceURL;

    private String user;

    private String password;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    public GlobalCompanyIdentifierServiceTemplate() {
	jaxbUtil = new WssSecurityUtil();
	webServiceTemplate = new WebServiceTemplate();
	Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
	marshaller.setContextPath("com.expd.service.global_company_identifier.v1");
	webServiceTemplate.setMarshaller(marshaller);
	webServiceTemplate.setUnmarshaller(marshaller);
    }

    @Override
    public GetOrganizationResponse getOrganizations(GetOrganizationRequest in) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
	GetOrganizations getOrganizationsReq = new GetOrganizations();
	getOrganizationsReq.setIn(in);
        System.out.println("Mahesh is testing getOrganizations ");
      return (GetOrganizationResponse) circuitBreaker.run(()->((com.expd.service.global_company_identifier.v1.GetOrganizationsResponse) webServiceTemplate
                .marshalSendAndReceive(globalCompanyIdentifierServiceURL, getOrganizationsReq,
                        new WebServiceMessageCallback() {
                            public void doWithMessage(WebServiceMessage webServiceMessage) {
                                try {
                                    SoapMessage soapMessage = (SoapMessage) webServiceMessage;
                                    JaxbContextUtil.getContext(SecurityHeaderType.class).createMarshaller().marshal(
                                            jaxbUtil.createWSSecurityHeader(user, password),
                                            soapMessage.getSoapHeader().getResult());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        })).getOut(),throwable -> getDefaultAlbumList());

	/*com.expd.service.global_company_identifier.v1.GetOrganizationsResponse operationResponse = (com.expd.service.global_company_identifier.v1.GetOrganizationsResponse) webServiceTemplate
	        .marshalSendAndReceive(globalCompanyIdentifierServiceURL, getOrganizationsReq,
	                new WebServiceMessageCallback() {
	                    public void doWithMessage(WebServiceMessage webServiceMessage) {
		                try {
		                    SoapMessage soapMessage = (SoapMessage) webServiceMessage;
                            JaxbContextUtil.getContext(SecurityHeaderType.class).createMarshaller().marshal(
	                                    jaxbUtil.createWSSecurityHeader(user, password),
	                                    soapMessage.getSoapHeader().getResult());
		                } catch (Exception e) {
		                    throw new RuntimeException(e);
		                }
	                    }
	                });
	return operationResponse.getOut();*/
    }

    public String getDefaultAlbumList() {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("fallback-album-list.json").toURI())));
        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public FindOrgRelationshipResponse findOrgRelationship(FindOrgRelationshipRequest in) {
	return null;
    }

    @Override
    public SetOrgRoleStatusResponse setOrgRoleStatus(SetOrgRoleStatusRequest in) {
	return null;
    }

    @Override
    public GetChildrenOrgRelationshipResponse getChildrenOrgRelationship(GetChildrenOrgRelationshipRequest in) {
	return null;
    }

    @Override
    public AddOrgInternalContactResponse addOrgInternalContact(AddOrgInternalContactRequest in) {
	return null;
    }

    @Override
    public CreateOrgRelationshipParentResponse createOrgRelationshipParent(CreateOrgRelationshipParentRequest in) {
	return null;
    }

    @Override
    public CreateOrgBranchAffiliationResponse createOrgBranchAffiliation(CreateOrgBranchAffiliationRequest in) {
	return null;
    }

    @Override
    public AffiliateAddressResponse affiliateAddress(AffiliateAddressRequest in) {
	return null;
    }

    @Override
    public GetProposalSummariesResponse getProposalSummaries(GetProposalSummariesRequest in) {
	return null;
    }

    @Override
    public CreateSimpleOrganizationResponse createSimpleOrganization(CreateSimpleOrganizationRequest in) {
	return null;
    }

    @Override
    public DeleteOrgAliasResponse deleteOrgAlias(DeleteOrgAliasRequest in) {
	return null;
    }

    @Override
    public ResaveOrganizationResponse resaveOrganization(ResaveOrganizationRequest in) {
	return null;
    }

    @Override
    public GetBillingRefTypesResponse getBillingRefTypes(GetBillingRefTypesRequest in) {
	return null;
    }

    @Override
    public CheckAddressesForAffiliatesResponse checkAddressesForAffiliates(CheckAddressesForAffiliatesRequest in) {
	return null;
    }

    @Override
    public DeleteOrgRoleAffiliationResponse deleteOrgRoleAffiliation(DeleteOrgRoleAffiliationRequest in) {
	return null;
    }

    @Override
    public ProcessCreditLimitResponse processCreditLimit(ProcessCreditLimitRequest in) {
	return null;
    }

    @Override
    public GetOrgIdTypesResponse getOrgIdTypes(GetOrgIdTypesRequest in) {
	return null;
    }

    @Override
    public GetBccsResponse getBccs(GetBccsRequest in) {
	return null;
    }

    @Override
    public DeleteOrgRelationshipResponse deleteOrgRelationship(DeleteOrgRelationshipRequest in) {
	return null;
    }

    @Override
    public GetCountriesResponse getCountries(GetCountriesRequest in) {
	return null;
    }

    @Override
    public GetPartsUsageOrganizationResponse getPartsUsageOrganizations(GetPartsUsageOrganizationRequest in) {
	return null;
    }

    @Override
    public MoveOrgRelationshipResponse moveOrgRelationship(MoveOrgRelationshipRequest in) {
	return null;
    }

    @Override
    public GetOrganizationSummaryResponse getOrganizationSummary(GetOrganizationSummaryRequest in) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        GetOrganizationSummary getOrganizationsReq = new GetOrganizationSummary();



        getOrganizationsReq.setIn(in);


     return   (GetOrganizationSummaryResponse)circuitBreaker.run(()->((com.expd.service.global_company_identifier.v1.GetOrganizationSummaryResponse) webServiceTemplate
                        .marshalSendAndReceive(globalCompanyIdentifierServiceURL, getOrganizationsReq,
                                new WebServiceMessageCallback() {
                                    public void doWithMessage(WebServiceMessage webServiceMessage) {
                                        try {
                                            SoapMessage soapMessage = (SoapMessage) webServiceMessage;
                                            JaxbContextUtil.getContext(SecurityHeaderType.class).createMarshaller().marshal(
                                                    jaxbUtil.createWSSecurityHeader(user, password),
                                                    soapMessage.getSoapHeader().getResult());
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                })).getOut(),throwable -> getDefaultAlbumList());

       /* com.expd.service.global_company_identifier.v1.GetOrganizationSummaryResponse operationResponse = (com.expd.service.global_company_identifier.v1.GetOrganizationSummaryResponse) webServiceTemplate
                .marshalSendAndReceive(globalCompanyIdentifierServiceURL, getOrganizationsReq,
                        new WebServiceMessageCallback() {
                            public void doWithMessage(WebServiceMessage webServiceMessage) {
                                try {
                                    SoapMessage soapMessage = (SoapMessage) webServiceMessage;
                                    JaxbContextUtil.getContext(SecurityHeaderType.class).createMarshaller().marshal(
                                            jaxbUtil.createWSSecurityHeader(user, password),
                                            soapMessage.getSoapHeader().getResult());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
        return operationResponse.getOut();

        */
    }

    @Override
    public ProcessDeniedPartyUpdateResponse processDeniedPartyUpdate(ProcessDeniedPartyUpdateRequest in) {
	return null;
    }

    @Override
    public SetOrgImporterRoleResponse setOrgImporterRole(SetOrgImporterRoleRequest in) {
	return null;
    }

    @Override
    public CalculateMatchScoreResponse calculateMatchScore(CalculateMachScoreRequest in) {
	return null;
    }

    @Override
    public LoadFamilyTreeResponse loadFamilyTree(LoadFamilyTreeRequest in) {
	return null;
    }

    @Override
    public CreateOrganizationResponse createOrganization(CreateOrganizationRequest in) {
	return null;
    }

    @Override
    public CheckGciNumberResponse checkGciNumber(CheckGciNumberRequest in) {
	return null;
    }

    @Override
    public UpdateOrgAliasResponse updateOrgAlias(UpdateOrgAliasRequest in) {
	return null;
    }

    @Override
    public GetEtmsOrganizationResponse getEtmsOrganization(GetEtmsOrganizationRequest in) {
	return null;
    }

    @Override
    public SaveExternalContactResponse saveExternalContact(SaveExternalContactRequest in) {
	return null;
    }

    @Override
    public CreateOrgRelationshipResponse createOrgRelationship(CreateOrgRelationshipRequest in) {
	return null;
    }

    @Override
    public GetAddressFormatsResponse getAddressFormats(GetAddressFormatsRequest in) {
	return null;
    }

    @Override
    public ProcessProposalResponse processProposal(ProcessProposalRequest in) {
	return null;
    }

    @Override
    public ValidateUdPromotionResponse validateUdPromotion(ValidateUdPromotionRequest in) {
	return null;
    }

    @Override
    public GetFamilyTreeResponse getFamilyTree(GetFamilyTreeRequest in) {
	return null;
    }

    @Override
    public SendDeniedPartyUpdateNoticeResponse sendDeniedPartyUpdateNotice(SendDeniedPartyUpdateNoticeRequest in) {
	return null;
    }

    @Override
    public GetFamilyTreeSuperOrdinateResponse getFamilyTreeSuperOrdinate(GetFamilyTreeSuperOrdinateRequest in) {
	return null;
    }

    @Override
    public CreateConsigneeResponse createConsignee(CreateConsigneeRequest in) {
	return null;
    }

    @Override
    public CreateGciConfigurationResponse createGciConfiguration(CreateGciConfigurationRequest in) {
	return null;
    }

    @Override
    public GeneratePrintableAddressResponse generatePrintableAddress(GeneratePrintableAddressRequest in) {
	return null;
    }

    @Override
    public EtmsInboundResponseResponse processUpdateEtmsClientResponse(EtmsInboundResponse in) {
	return null;
    }

    @Override
    public SetOrgBranchRoleResponse setOrgBranchRole(SetOrgBranchRoleRequest in) {
	return null;
    }

    @Override
    public FindOrganizationResponse findOrganization(FindOrganizationRequest in) {
	return null;
    }

    @Override
    public GetGciConfigurationsResponse getGciConfigurations(GetGciConfigurationsRequest in) {
	return null;
    }

    @Override
    public DeleteOrgAddressResponse deleteOrgAddress(DeleteOrgAddressRequest in) {
	return null;
    }

    @Override
    public RemoveOrgInternalContactResponse removeOrgInternalContact(RemoveOrgInternalContactRequest in) {
	return null;
    }

    @Override
    public CreateOrgAddressResponse createOrgAddress(CreateOrgAddressRequest in) {
	return null;
    }

    @Override
    public GetUserProfilesResponse getUserProfiles(GetUserProfilesRequest in) {
	return null;
    }

    @Override
    public GetParentOrgRelationshipResponse getParentOrgRelationship(GetParentOrgRelationshipRequest in) {
	return null;
    }

    @Override
    public CreateOrgAliasResponse createOrgAlias(CreateOrgAliasRequest in) {
	return null;
    }

    @Override
    public SendEdiRecordsToEtmsResponse sendEdiRecordsToEtms(SendEdiRecordsToEtmsRequest in) {
	return null;
    }

    @Override
    public CheckOrgRelationshipResponse checkOrgRelationship(CheckOrgRelationshipRequest in) {
	return null;
    }

    @Override
    public UpdateGciConfigurationResponse updateGciConfiguration(UpdateGciConfigurationRequest in) {
	return null;
    }

    @Override
    public ValidateAddressResponse validateAddress(ValidateAddressRequest in) {
	return null;
    }

    @Override
    public ValidateBccReplacementResponse validateBccReplacement(ValidateBccReplacementRequest in) {
	return null;
    }

    @Override
    public PreliminaryBccValidationResponse performPreliminaryBccValidation(PreliminaryBccValidationRequest in) {
	return null;
    }

    @Override
    public SaveSuspectedDuplicateLogResponse saveSuspectedDuplicate(SaveSuspectedDuplicateLogRequest in) {
	return null;
    }

    @Override
    public ValidateOrganizationResponse validateOrganization(ValidateOrganizationRequest in) {
	return null;
    }

    @Override
    public GetStateProvincesResponse getStateProvinces(GetStateProvincesRequest in) {
	return null;
    }

    @Override
    public UpdateTasksResponse updateTasks(UpdateTasksRequest in) {
	return null;
    }

    @Override
    public GetChildrenOrgRelationshipWithOrgListResponse getChildrenOrgRelationshipWithOrgList(
            GetChildrenOrgRelationshipWithOrgListRequest in) {
	return null;
    }

    @Override
    public SendExpinToEtmsResponse sendExpinToEtms(SendExpinToEtmsRequest in) {
	return null;
    }

    @Override
    public GetHistoryResponse getHistory(GetHistoryRequest in) {
	return null;
    }

    @Override
    public SaveGciConfigurationResponse saveGciConfiguration(SaveGciConfigurationRequest in) {
	return null;
    }

    @Override
    public UpdateOrganizationResponse updateOrganization(UpdateOrganizationRequest in) {
	return null;
    }

    @Override
    public SaveOrgRoleAffiliationResponse saveOrgRoleAffiliation(SaveOrgRoleAffiliationRequest in) {
	return null;
    }

    @Override
    public GetSuspectedDuplicatesForResponse getSuspectedDuplicatesFor(GetSuspectedDuplicatesForRequest in) {
	return null;
    }

    @Override
    public UpdateOrgAddressResponse updateOrgAddress(UpdateOrgAddressRequest in) {
	return null;
    }

    @Override
    public PublishGciUpdateResponse publishGciUpdate(PublishGciUpdateRequest in) {
	return null;
    }

    @Override
    public CreateOrgRelationshipChildrenResponse createOrgRelationshipChildren(
            CreateOrgRelationshipChildrenRequest in) {
	return null;
    }

    @Override
    public GetIndustriesResponse getIndustries(GetIndustriesRequest in) {
	return null;
    }

    @Override
    public SendExpinToCdbResponse sendExpinToCdb(SendExpinToCdbRequest in) {
	return null;
    }

    @Override
    public GetSuspectedDuplicatesResponse getSuspectedDuplicates(GetSuspectedDuplicatesRequest in) {
	return null;
    }

    @Override
    public GetBranchesResponse getBranches(GetBranchesRequest in) {
	return null;
    }

    public String getUser() {
	return user;
    }

    public void setUser(String user) {
	this.user = user;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getGlobalCompanyIdentifierServiceURL() {
	return globalCompanyIdentifierServiceURL;
    }

    public void setGlobalCompanyIdentifierServiceURL(String globalCompanyIdentifierServiceURL) {
	this.globalCompanyIdentifierServiceURL = globalCompanyIdentifierServiceURL;
    }

    @Override
    public CreateEcsOrganizationsResponse createEcsOrganizations(CreateEcsOrganizationsRequest in) {
	return null;
    }

}
