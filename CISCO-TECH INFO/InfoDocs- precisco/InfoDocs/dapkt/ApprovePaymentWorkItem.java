package com.db.gpf.csw.globalpayments.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import com.db.gpf.csw.globalpayments.constants.ApprovePaymentsReportConstants;
import com.db.gpf.csw.java.common.coverage.ICoverable;
import com.db.gpf.csw.java.common.database.model.CaseApprovalStep;
import com.db.gpf.csw.java.common.tibco.utils.CasePriorityConstants;
import com.db.gpf.csw.java.common.tibco.workitem.TibcoCacheWorkItem;
import com.db.gpf.csw.java.common.tibco.workitem.WorkItemApproval;
import com.db.gpf.csw.service.util.ApprovePaymentsConstants;
import com.db.gpf.csw.tibcocache.service.CswVWorkItem;
import com.tibco.bpm.sso.types.VACase;

import de.aixcept.flex2.annotations.ActionScript;
import de.aixcept.flex2.annotations.ActionScriptProperty;
/**
 * 
 * @author vikram.bethu
 * 
 */
@ActionScript(bindable = true, remoteObject = true)
public class ApprovePaymentWorkItem extends TibcoCacheWorkItem  {

	private static final Log log = LogFactory.getLog(ApprovePaymentWorkItem.class);
	private String counterPartyType;
	private String tradeType;
	private String enteredBy;
	private String accountDescription;
	private String clientDescription;
	private String fundDescription;
	private String riskRulesResult;
	private Long accountId;
	private Long clientId;
	private Long fundId;
	private Date valueDate;
	private Date fundingDeadline;
	private Date paymentDeadline;
	private Double tracerCashAvailable;
	private Double nominal;
	private Double ccyToReportingCcyFxRate;
	private Double usdEquivalent;
	private String ccy;
	private Long tracerRawrefid;
	private String accountSourceId;
	private String business;
	private Boolean isForwardDatedUSPBPayment = false;
	private String rptgCcy;
	private Date createdDt;
	private Date closedDt;
	private String stepNameCsv;
	private Date stepArrivalTime;
	private String stepReleaseBy;
	private Date stepReleaseTime;
	private String region;
	private String paymentType;
	private Date postedDate;
	private Payment payment;//Vicky
	private boolean isActive;
	private String username;
	private List<CaseApprovalStep> definedApprovals;

	private PaymentWorkItem paymentWorkItem;
	
	private static final long serialVersionUID = 7569218317368079085L;
	private static HashMap<String, String> fieldMap = new HashMap<String, String>();
	private static String PRIORITY_DESC_FOR_CLOSED_CASES = "N/A";
	private static String STEP_CLOSED = "CLOSED";
	private static String IPB_BUSINESS = "IPB";
	private static String USPB_BUSINESS = "USPB";

	/********************** New_Payments - START ******************************/
	public static final long ONE_MINUTE = 60;
	public static final long ONE_HOUR = ONE_MINUTE * 60;
	public static final long ONE_DAY = ONE_HOUR * 24;
	public static final long FOUR_DAYS = ONE_DAY * 4;
	public static final long FOUR_HOURS = ONE_HOUR * 4;
	public static final long THIRTY_MINUTES = ONE_MINUTE * 30;
	/********************** New_Payments - END ******************************/
	static {
		fieldMap.put("CLNT_ID", "clientId");
		fieldMap.put("CLNT_NME", "clientDescription");
		fieldMap.put("FUND_ID", "fundId");
		fieldMap.put("FUND_NME", "fundDescription");
		fieldMap.put("ACCT_ID", "accountId");
		fieldMap.put("ACCT_NME", "accountDescription");
		fieldMap.put("ACCT_SRC_ID", "accountSourceId");
		fieldMap.put("NOMINAL", "nominal");
		fieldMap.put("AMOUNT", "usdEquivalent");
		fieldMap.put("VALUEDATE", "valueDate");
		fieldMap.put("FUNDINGDEADLINE", "fundingDeadline");
		fieldMap.put("PAYMENTDEADLINE", "paymentDeadline");
		fieldMap.put("TR_CASH_AVAIL", "tracerCashAvailable");
		fieldMap.put("RISKPASSFAIL", "riskRulesResult");
		fieldMap.put("CCY_2_RPT_FXRTE", "ccyToReportingCcyFxRate");
		fieldMap.put("CPTYTYPE", "counterPartyType");
		fieldMap.put("TRADETYPE", "tradeType");
		fieldMap.put("ENTEREDBY", "enteredBy");
		fieldMap.put("CCY", "ccy");
		fieldMap.put("APPROVAL", "approvalInformation");
		fieldMap.put("TRACERRAWREFID", "tracerRawrefid");
		fieldMap.put("BUSINESS", "business");
		fieldMap.put("PAY_TYP", "paymentType");
		fieldMap.put("POSTED_DTE", "postedDate");

	}

	@Override
	public HashMap<String, String> getFieldMap() {
		return fieldMap;
	}
	
	

	public Payment getPayment() {
		return payment;
	}



	public void setPayment(Payment payment) {
		this.payment = payment;
	}



	public boolean isActive() {
		return isActive;
	}



	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}



	public String getUsername() {
		return username;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public List<CaseApprovalStep> getDefinedApprovals() {
		return definedApprovals;
	}



	public void setDefinedApprovals(List<CaseApprovalStep> definedApprovals) {
		this.definedApprovals = definedApprovals;
	}



	@Override
	protected void setVisibility(List<CaseApprovalStep> definedApprovals,
			String username) {
		// Artifact artf760350 : Payment approval rule changes for CSR approval
		// Below code is commented to allow same CSR to approve a given payment
		// super.setVisibility(definedApprovals, username);

		if (username.equals(this.getEnteredBy())) {
			this.setIsVisible(false);
		}

		// For USD USPB payments if it's forward dated then set it as an alert
		if (this.getBusiness() != null && !this.getBusiness().isEmpty()) {
			if (this.getBusiness().equals(USPB_BUSINESS)) {
				if (this.getCcy().equals("USD")) {
					// For some reason the date that's coming back from Tibco is
					// set to midnight UTC
					// Causes problems when run on an EST JVM, hence the
					// explicit mapping to UTC here.
					LocalDate valueDate = new LocalDate(this.getValueDate()
							.getTime(), DateTimeZone.UTC);
					LocalDate today = new LocalDate((new Date()).getTime());
					if (valueDate.isAfter(today)) {
						// this.setIsVisible(false);
						this.setIsForwardDatedUSPBPayment(true);
					}
				}
			}
		}

		// Changes Start : Artifact artf760350 : Payment approval rule changes
		// for CSR approval
		// For Cases in step MGTAPV,MGTAPV2,MGTAPV3..... Same user cannot take
		// same/similar action(MGTAPV,MGTAPV2,MGTAPV3).. Hence set as alert
		if (this.getStepName() != null) {
			if (this.getStepName().toUpperCase().contains(
					ApprovePaymentsConstants.MGTAPV_STP)) {

				boolean isVisibleFlag = true;
				for (WorkItemApproval approvals : getApprovals()) {
					if (definedApprovals.size() != 0) {// Defined Approvals
														// Present
						for (CaseApprovalStep definedStep : definedApprovals) {
							if (definedStep
									.getStepName()
									.toUpperCase()
									.contains(
											ApprovePaymentsConstants.MGTAPV_STP)
									&& approvals
											.getStepName()
											.toUpperCase()
											.contains(
													ApprovePaymentsConstants.MGTAPV_STP)) {
								if (username.equals(approvals.getApprover())) {
									if (definedStep.getUserAction().equals(
											approvals.getAction())) {
										this.setIsVisible(false);
										isVisibleFlag = false;
										break;
									}
								}
							}
						}
					} else { // Default logic when no case approval steps
						if (approvals.getStepName().toUpperCase().contains(
								ApprovePaymentsConstants.MGTAPV_STP)
								&& username.equals(approvals.getApprover())) {
							this.setIsVisible(false);
							isVisibleFlag = false;
							break;
						}
					}
					if (!isVisibleFlag) {
						break;
					}// End the loop
				}

			}

			if (this.getStepName().toUpperCase().contains(
					ApprovePaymentsConstants.CASE_APPROVED)) {
				this.setIsVisible(false);
			}
		}
		// Changes End : Artifact artf760350 : Payment approval rule changes for
		// CSR approval

	}

	public boolean isDynamicQueue() {
		return false;
	}

	public ApprovePaymentWorkItem(CswVWorkItem workitem) {
		super(workitem);
		setCaseFields(workitem.getCaseFields().getVField());
	}

	// Active Cases
	public ApprovePaymentWorkItem(CswVWorkItem workItem, String username,
			List<CaseApprovalStep> definedApprovals) {
		super(workItem);
		setCaseFields(workItem.getCaseFields().getVField());
		parseApprovals();
		setVisibility(definedApprovals, username);
	}

	/********************** New_Payments - START ******************************/
	/**
	 * get active workitems and binds into VO (Reff:ApprovePaymentWorkItem.as)
	 */
	public ApprovePaymentWorkItem(Payment payment, boolean isActive,
			String username, List<CaseApprovalStep> definedApprovals ) {
		this.setPayment(payment);
		this.setActive(isActive);
		this.setUsername(username);
		this.setDefinedApprovals(definedApprovals);
		paymentWorkItem = new PaymentWorkItem();
		this.setCcy(payment.getCurrency());
		this.setNominal(payment.getNominalAmount().doubleValue());
		this.setCaseNumber(payment.getCaseId());
		this.setClientId(payment.getMasterCaseRecord().getParentId());
		this.setFundingDeadline(payment.getFundingDeadline());
		this.setPaymentDeadline(payment.getPaymentDeadline());
		this.setValueDate(payment.getValueDate());
		this.setUsdEquivalent(payment.getUsdEquivalent().doubleValue());
		// for lock case
		log.info("Locked By---->>"+payment.getLockedCases().getLockedBy());
		 this.setLockedBy(payment.getLockedCases().getLockedBy());
		 if(null != payment.getLockedCases().getLockedBy()) {
			 this.setLocked(true);
			 this.setLongLocked(true);
			 }
		 else{
			 this.setLocked(false);
			 this.setLongLocked(false);
			 } 
		 log.info(" is locked:::"+isLocked());
		 
		// this.setLongLocked(true);
		// TODO
		// Error in the below line when the (CASE_MST.case_stp_id == null) -->
		// Need to investigate further
		if (payment.getMasterCaseRecord().getCaseStep() != null) {
			this.setStepName(payment.getMasterCaseRecord().getCaseStep()
					.getStepName());
		} else {
			log.debug("getCaseStep is null for case_num :"
					+ payment.getMasterCaseRecord().getCaseNumber());
		}
		this.setAccountId(payment.getMasterCaseRecord().getAccountId());
		this.setAccountDescription(payment.getMasterCaseRecord().getAccountName());
		this.setTeamName(payment.getEnteredBy());
		this.setEnteredBy(payment.getEnteredBy());
		this.setTradeType(payment.getTradeType());
		this.setCustodyAccountStatus(payment.getTradeType());
		this.setCashAvailableStatus(payment.getTradeType());
		try {
			this.setParentId(payment.getMasterCaseRecord().getParentId());
			this.setClientDescription(payment.getMasterCaseRecord()
					.getParentName());

		} catch (Exception e) {
			log.error("Value of caseId is:" + this.getCaseNumber()
					+ ":Value of payment.getMasterCaseRecord is:"
					+ payment.getMasterCaseRecord().getParentId() + ":");
		}
		if (isActive) {
			this.setClosed(false);
		} else {
			this.setClosed(true);
		}
		int priorityTemp = getPriority(this.getFundingDeadline());
		this.setPriority(getPriority(this.getFundingDeadline()));
		this.setPriorityDesc(getPriorityDescriptionFromValue(Integer.valueOf(priorityTemp)));
		this.setRptgCcy(payment.getReportingCurrency());
		this.setCreatedDt(payment.getMasterCaseRecord().getCreatedDate());
		this.setClosedDt(payment.getMasterCaseRecord().getCloseDate());
		this.setAccountSourceId(payment.getSourceId());
		this.setPaymentType(payment.getPaymentType());
		this.setStepArrivalTime(payment.getMasterCaseRecord().getCreatedDate());
		this.setStepReleaseTime(payment.getMasterCaseRecord().getModifiedDate());
		this.setStepReleaseBy(username);
		this.setRegion(payment.getRegion());
		this.setPostedDate(payment.getPostedDate());
		this.setWorkflowID(paymentWorkItem.getWorkflowID());
		this.setProcedureName(paymentWorkItem.getProcedureName());
		this.setWorkItemTag(paymentWorkItem.getWorkItemTag());
		this.setWorkQTag(paymentWorkItem.getWorkQTag());
		log.info("in active workitem class paymentWorkItem.getWorkflowID()--------->"+paymentWorkItem.getWorkflowID());
		log.info("in active workitem class paymentWorkItem.getProcedureName()--------->"+paymentWorkItem.getProcedureName());
		log.info("in active workitem class paymentWorkItem.getWorkItemTag()--------->"+paymentWorkItem.getWorkItemTag());
		log.info("in active workitem class paymentWorkItem.getWorkQTag()--------->"+paymentWorkItem.getWorkQTag());
		this.setClientId(payment.getMasterCaseRecord().getParentId());
		if (payment.getRegion().equalsIgnoreCase("I")) {
			this.setBusiness(IPB_BUSINESS);
		} else {
			this.setBusiness(USPB_BUSINESS);
		}
		log.info("risk object===>"+payment.getRiskReviews());
		log.info("riskreview size ===========>"+payment.getRiskReviews().size());
		Set<RiskReview> riskReview = payment.getRiskReviews();
		log.info("riskreview is empty ===========>"+riskReview.isEmpty());
		
		if(payment.getRiskReviews() != null){
			riskReview = payment.getRiskReviews();
			Iterator<RiskReview> reviewItr = riskReview.iterator();
			RiskReview reviewResult = null;
			while( reviewItr.hasNext() ){
				reviewResult = reviewItr.next();
				if( reviewResult != null ){
					log.info("Tracer cash available------->"+reviewResult.getTracerCashAvailable());
					this.setTracerCashAvailable(reviewResult.getTracerCashAvailable());
					log.info("risk status------->"+reviewResult.getOverallStatus().getCode());
					this.setRiskRulesResult(reviewResult.getOverallStatus().getCode());					
				}
				
			}
		}
		setVisibility(definedApprovals, username);

	}

	

	/**
	 * Calculates time difference between Current time and deadline date in ms
	 * 
	 * @param dbDeadlineDate
	 * @return
	 */
	public static Long getTimeDiffWithCurrTimeInSecs(Date dbDeadlineDate) {
		if (dbDeadlineDate == null)
			return null;
		Calendar inputDate = Calendar.getInstance();
		inputDate.setTime(dbDeadlineDate);
		log.debug("getTimeDiffWithCurrTimeInSecs (): dbDeadlineDate :"
				+ dbDeadlineDate);
		Calendar currDate = Calendar.getInstance();
		currDate.setTimeZone(inputDate.getTimeZone());
		log.debug("getTimeDiffWithCurrTimeInSecs ():curr time :"
				+ currDate.getTime());
		Long dateDiff = inputDate.getTimeInMillis()
				- currDate.getTimeInMillis();
		return dateDiff.longValue() / 1000; // seconds difference

	}

	/**
	 * getting date value and computing as priority
	 * 
	 * @param fundindDeadLine
	 *            :Date
	 */
	private short getPriority(final Date fundingDeadlineDate) {
		// Long dateDiffTemp =
		// CAUtil.getTimeDiffWithCurrTimeInSecs(anItem.getDbDeadlineDate());
		short priority = 0;

		Long dateDiff = null;

		dateDiff = ApprovePaymentWorkItem
				.getTimeDiffWithCurrTimeInSecs(fundingDeadlineDate);

		log.debug("getPriority():date difference:" + dateDiff);

		if (null == dateDiff)
			priority = CasePriorityConstants.NA.getRangeHigh().shortValue();
		else if ((dateDiff <= FOUR_HOURS && dateDiff > THIRTY_MINUTES))
			priority = CasePriorityConstants.Normal.getRangeHigh().shortValue();
		else
			/* if (dateDiff <= THIRTY_MINUTES) */
			priority = CasePriorityConstants.High.getRangeHigh().shortValue();

		return priority;
	}

	/********************** New_Payments - END ******************************/

	// Closed Cases
	public ApprovePaymentWorkItem(VACase c) {
		super(c);
		setCaseFields(c.getCaseFields().getVField());
	}

	/**
	 * Set the values from Payment obj to ApprovePaymentWorkItem for closed
	 * cases
	 * 
	 * @param payment
	 */
	public ApprovePaymentWorkItem(Payment payment) {
		this.setCcy(payment.getCurrency());
		this.setNominal(payment.getNominalAmount().doubleValue());
		this.setCaseNumber(payment.getCaseId());
		this.setFundingDeadline(payment.getFundingDeadline());
		this.setPaymentDeadline(payment.getPaymentDeadline());
		this.setValueDate(payment.getValueDate());
		this.setUsdEquivalent(payment.getUsdEquivalent().doubleValue());
		this.setStepName(STEP_CLOSED);
		this.setAccountId(payment.getMasterCaseRecord().getAccountId());
		this.setAccountDescription(payment.getMasterCaseRecord()
				.getAccountName());
		this.setParentId(payment.getMasterCaseRecord().getParentId());
		this
				.setClientDescription(payment.getMasterCaseRecord()
						.getParentName());
		this.setPriorityDesc(PRIORITY_DESC_FOR_CLOSED_CASES);
		this.setClosed(true);

		this.setClientId(payment.getMasterCaseRecord().getParentId());
		if (payment.getRegion().equalsIgnoreCase("I")) {
			this.setBusiness(IPB_BUSINESS);
		} else {
			this.setBusiness(USPB_BUSINESS);
		}

		/******** New_Payments - START ******************************/
		// Make all the Closed Cases as Alerts and not Tasks in GUI
		this.setIsVisible(false);
		/******** New_Payments - END ******************************/

	}

	/**
	 * Set the values from Payment obj to ApprovePaymentWorkItem for CSV
	 * 
	 * 
	 * @param payment
	 */
	public ApprovePaymentWorkItem(Payment payment, boolean isActive,
			Date stepArrivalTime, Date stepReleaseTime, String stepReleaseBy,
			String stpNme, String region) {

		this.setCcy(payment.getCurrency());
		this.setNominal(payment.getNominalAmount().doubleValue());
		this.setCaseNumber(payment.getCaseId());
		this.setFundingDeadline(payment.getFundingDeadline());
		this.setPaymentDeadline(payment.getPaymentDeadline());
		this.setValueDate(payment.getValueDate());
		this.setUsdEquivalent(payment.getUsdEquivalent().doubleValue());
		this.setStepNameCsv(stpNme);// payment.getMasterCaseRecord().getCaseStep().getStepName());}
		this.setAccountId(payment.getMasterCaseRecord().getAccountId());
		this.setAccountDescription(payment.getMasterCaseRecord()
				.getAccountName());
		// TODO NEW-PAYMENT
		/*
		 * this.setParentId(100); this.setClientDescription("Testing12");
		 */

		if (isActive) {
			this.setClosed(false);
		} else {
			this.setClosed(true);
		}

		this.setRptgCcy(payment.getReportingCurrency());
		this.setCreatedDt(payment.getMasterCaseRecord().getCreatedDate());
		this.setClosedDt(payment.getMasterCaseRecord().getCloseDate());
		this.setStepArrivalTime(stepArrivalTime);
		this.setStepReleaseTime(stepReleaseTime);
		this.setStepReleaseBy(stepReleaseBy);
		this.setRegion(region);

		this.setClientId(payment.getMasterCaseRecord().getParentId());
		if (payment.getRegion().equalsIgnoreCase("I")) {
			this.setBusiness(IPB_BUSINESS);
		} else {
			this.setBusiness(USPB_BUSINESS);
		}

	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getRegion() {
		return this.region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getRptgCcy() {
		return this.rptgCcy;
	}

	public void setRptgCcy(String ccy) {
		this.rptgCcy = ccy;
	}

	public String getStepNameCsv() {
		return this.stepNameCsv;
	}

	public void setStepNameCsv(String stepNameCsv) {
		this.stepNameCsv = stepNameCsv;
	}

	public Date getCreatedDt() {
		return createdDt;
	}

	public void setCreatedDt(Date createdDt) {
		this.createdDt = createdDt;
	}

	public Date getClosedDt() {
		return closedDt;
	}

	public void setClosedDt(Date closedDt) {
		this.closedDt = closedDt;
	}

	public Date getStepArrivalTime() {
		return stepArrivalTime;
	}

	public void setStepArrivalTime(Date stepArrivalTime) {
		this.stepArrivalTime = stepArrivalTime;
	}

	public Date getStepReleaseTime() {
		return stepReleaseTime;
	}

	public void setStepReleaseTime(Date stepReleaseTime) {
		this.stepReleaseTime = stepReleaseTime;
	}

	public String getStepReleaseBy() {
		return this.stepReleaseBy;
	}

	public void setStepReleaseBy(String stepReleaseBy) {
		this.stepReleaseBy = stepReleaseBy;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getCounterPartyType() {
		return counterPartyType;
	}

	public void setCounterPartyType(String counterPartyType) {
		this.counterPartyType = counterPartyType;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getEnteredBy() {
		return enteredBy;
	}

	public void setEnteredBy(String enteredBy) {
		this.enteredBy = enteredBy;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getAccountDescription() {
		return accountDescription;
	}

	public void setAccountDescription(String accountDescription) {
		this.accountDescription = accountDescription;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getClientDescription() {
		return clientDescription;
	}

	public void setClientDescription(String clientDescription) {
		this.clientDescription = clientDescription;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getFundDescription() {
		return fundDescription;
	}

	public void setFundDescription(String fundDescription) {
		this.fundDescription = fundDescription;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getRiskRulesResult() {
		return riskRulesResult;
	}

	public void setRiskRulesResult(String riskRulesResult) {
		this.riskRulesResult = riskRulesResult;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Long getFundId() {
		return fundId;
	}

	public void setFundId(Long fundId) {
		this.fundId = fundId;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Date getValueDate() {
		return valueDate;
	}

	public void setValueDate(Date valueDate) {
		this.valueDate = valueDate;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Double getTracerCashAvailable() {
		return tracerCashAvailable;
	}

	public void setTracerCashAvailable(Double tracerCashAvailable) {
		this.tracerCashAvailable = tracerCashAvailable;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Double getNominal() {
		return nominal;
	}

	public void setNominal(Double nominal) {
		this.nominal = nominal;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Double getCcyToReportingCcyFxRate() {
		return ccyToReportingCcyFxRate;
	}

	public void setCcyToReportingCcyFxRate(Double ccyToReportingCcyFxRate) {
		this.ccyToReportingCcyFxRate = ccyToReportingCcyFxRate;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Double getUsdEquivalent() {
		return usdEquivalent;
	}

	public void setUsdEquivalent(Double usdEquivalent) {
		this.usdEquivalent = usdEquivalent;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getCcy() {
		return this.ccy;
	}

	public void setCcy(String ccy) {
		this.ccy = ccy;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Date getFundingDeadline() {
		return fundingDeadline;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Date getPaymentDeadline() {
		return paymentDeadline;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public void setFundingDeadline(Date fundingDeadline) {
		this.fundingDeadline = fundingDeadline;
	}

	public void setPaymentDeadline(Date paymentDeadline) {
		this.paymentDeadline = paymentDeadline;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getCashAvailableStatus() {
		if (getTradeType().toLowerCase().equals("cp")) {

			if (getTracerCashAvailable() >= 0) {
				return "green";
			}

			return "red";
		}
		return "n/a";
	}

	public void setCashAvailableStatus(String cashAvailableStatus) {

	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getCustodyAccountStatus() {
		if (getTradeType().toLowerCase().equals("cp")) {
			if (!getAccountDescription().toLowerCase().contains("custody")) {
				return "green";
			}
			return "red";
		}
		return "n/a";
	}

	// for flex serialization
	public void setCustodyAccountStatus(String custodyAccountStatus) {

	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public boolean isThirdParty() {
		boolean thirdPartyOrNot = false;
		if (this.getCounterPartyType() != null
				&& this.getCounterPartyType().toUpperCase().equals(
						"THIRD PARTY")) {
			thirdPartyOrNot = true;
		}
		return thirdPartyOrNot;
	}

	// for flex serialization
	public void setThirdParty(boolean thirdPartyOrNot) {

	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Long getTracerRawrefid() {
		return tracerRawrefid;
	}

	public void setTracerRawrefid(Long tracerRawrefid) {
		this.tracerRawrefid = tracerRawrefid;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getAccountSourceId() {
		return accountSourceId;
	}

	public void setAccountSourceId(String accountSourceId) {
		this.accountSourceId = accountSourceId;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Date getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public String getRiskRulesStatus() {
		String riskRuleStatus = "";
		if (getBusiness().equalsIgnoreCase("IPB")) {
			if (getTradeType().toLowerCase().equals("cp")) {
				if (getRiskRulesResult().toLowerCase().equals("pass")) {
					riskRuleStatus = "green";
				} else if (getRiskRulesResult().toLowerCase().equals("fail")) {
					riskRuleStatus = "red";
				}
			} else { // CR
				riskRuleStatus = "n/a";
			}
		} else { // USPB
			if (getRiskRulesResult().toLowerCase().equals("pass")) {
				riskRuleStatus = "green";
			} else if (getRiskRulesResult().toLowerCase().equals("fail")) {
				riskRuleStatus = "red";
			}
		}
		return riskRuleStatus;
	}

	public void setRiskRulesStatus(String riskRulesStatus) {

	}

	@ActionScriptProperty(read = true, write = true, bindable = true)
	public Boolean getIsForwardDatedUSPBPayment() {
		return isForwardDatedUSPBPayment;
	}

	public void setIsForwardDatedUSPBPayment(Boolean isForwardDatedUSPBPayment) {
		this.isForwardDatedUSPBPayment = isForwardDatedUSPBPayment;
	}

	public Long getCaseId() {
		return super.getCaseNumber();
	}

	public boolean getIsClosed() {
		return super.isClosed();
	}

	public StringBuffer toCSV() {
		StringBuffer detailCSV = new StringBuffer();

		TimeZone gmtTZ = TimeZone.getTimeZone("EST");
		if (this.getRegion() != null) {
			if (this.getRegion().equalsIgnoreCase("EURO")) {
				gmtTZ = TimeZone.getTimeZone("GMT");
				// log.info("Region: ->"+this.getRegion()+"  Time Zone: -> "+gmtTZ);
			} else if (this.getRegion().equalsIgnoreCase("Asia")) {
				gmtTZ = TimeZone.getTimeZone("Asia/Singapore");
				// log.info("Region: ->"+this.getRegion()+"  Time Zone: -> "+gmtTZ);
			}
		}
		// log.info("Region: ->"+this.getRegion()+"  Time Zone: -> "+gmtTZ.getDisplayName());
		// log.info("Step Name "+this.getStepNameCsv());

		DateFormat df = ApprovePaymentsReportConstants.csvDateTimeFormat;
		df.setTimeZone(gmtTZ);

		Calendar today = Calendar.getInstance();
		Date todayDate = today.getTime();

		/*
		 * detailCSV.append(ApprovePaymentsReportConstants.csvDateFormat
		 * .format(todayDate));
		 * detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);
		 */

		/*
		 * if (getIsVisible()!=null){ if(getIsVisible()==true) {
		 * detailCSV.append("TASK");
		 * detailCSV.append(ApprovePaymentsReportConstants.csvSeperator); }else{
		 * detailCSV.append("ALERT");
		 * detailCSV.append(ApprovePaymentsReportConstants.csvSeperator); } }
		 */

		// Case Number
		if (this.getCaseNumber() != 0)
			detailCSV.append(this.getCaseNumber());
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Parent name
		if (this.getClientDescription() != null)
			detailCSV.append(addQuotes(this.getClientDescription()));
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Accnt Name
		if (this.getAccountDescription() != null)
			detailCSV.append(addQuotes(this.getAccountDescription()));
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		/*
		 * if (this.getFundDescription() != null)
		 * detailCSV.append(addQuotes(this.getFundDescription()));
		 * detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);
		 */

		// Curr
		if (this.getCcy() != null)
			detailCSV.append(this.getCcy());
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Reporting Curr
		if (this.getRptgCcy() != null)
			detailCSV.append(this.getRptgCcy());

		// detailCSV.append("RPT CURR");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Nominal
		if (this.getNominal() != null)
			detailCSV.append(this.getNominal());
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// $ Equiv
		if (this.getUsdEquivalent() != null)
			detailCSV.append(this.getUsdEquivalent());
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// ValueDate
		if (this.getValueDate() != null) {
			// log.info("Value date B4: "+this.getValueDate());
			// log.info("Value date After: "+df.format(this.getValueDate()));
			detailCSV.append(df.format(this.getValueDate()));
		}
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Funding Deadline
		if (this.getFundingDeadline() != null) {// log.info("getFundingDeadline date B4: "+this.getFundingDeadline());
			// log.info("getFundingDeadline date After: "+df.format(this.getFundingDeadline()));
			detailCSV.append(df.format(this.getFundingDeadline()));
		} else
			detailCSV.append("");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Payment Deadline
		if (this.getPaymentDeadline() != null) {
			// log.info("getPaymentDeadline date B4: "+this.getPaymentDeadline());
			// log.info("getPaymentDeadline date After: "+df.format(this.getPaymentDeadline()));
			detailCSV.append(df.format(this.getPaymentDeadline()));
		} else
			detailCSV.append("");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Business
		if (this.getBusiness() != null)
			detailCSV.append(this.getBusiness());
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Created date
		if (this.getCreatedDt() != null) {
			// log.info("getCreatedDt date B4: "+this.getCreatedDt());
			// log.info("getCreatedDt date After: "+df.format(this.getCreatedDt()));
			detailCSV.append(df.format(getCreatedDt()));
		}
		// detailCSV.append("CreatedDT");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Closed Dt
		if (this.isClosed())
			if (this.getClosedDt() != null) {
				// log.info("getClosedDt date B4: "+this.getClosedDt());
				// log.info("getClosedDt date After: "+df.format(this.getClosedDt()));
				detailCSV.append(df.format(getClosedDt()));
			}
		// detailCSV.append("ClosedDT");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Step Arrival time
		if (this.getStepArrivalTime() != null) {
			// log.info("getStepArrivalTime date B4: "+this.getStepArrivalTime());
			// log.info("getStepArrivalTime date After: "+df.format(this.getStepArrivalTime()));
			detailCSV.append(df.format(this.getStepArrivalTime()));
		}
		// detailCSV.append("Step Arrival Time");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Step Release time
		if (this.getStepReleaseTime() != null) {
			// log.info("getStepReleaseTime date B4: "+this.getStepReleaseTime());
			// log.info("getStepReleaseTime date After: "+df.format(this.getStepReleaseTime()));
			detailCSV.append(df.format(this.getStepReleaseTime()));
		}
		// detailCSV.append("Step Release Time");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Step Tim Difference
		// Step Release time
		if (this.getStepArrivalTime() != null
				&& this.getStepReleaseTime() != null) {
			SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
			Date d1 = null;
			Date d2 = null;
			try {
				d1 = format.parse(format.format(this.getStepArrivalTime()));
				d2 = format.parse(format.format(this.getStepReleaseTime()));
			} catch (ParseException e) {
				log.error("Date exception for case " + this.getCaseNumber()
						+ " D1: " + this.getStepArrivalTime() + "  D2= "
						+ this.getStepReleaseTime() + "\n" + e);
				log.info("Date exception for case " + this.getCaseNumber()
						+ " D1: " + this.getStepArrivalTime() + "  D2= "
						+ this.getStepReleaseTime() + "\n" + e);
				// e.printStackTrace();
			}
			// Get msec from each, and subtract.
			long diff = 0;
			if (d1 != null && d2 != null)
				diff = d2.getTime() - d1.getTime();
			// long diffSeconds = diff / 1000;
			// long diffMinutes = diff / (60 * 1000);
			long millis = diff % 1000;
			diff /= 1000;
			long seconds = diff % 60;
			diff /= 60;
			long minutes = diff % 60;
			diff /= 60;
			long diffHours = diff % 24; // diff / (60 * 60 * 1000);
			diff /= 24;
			long diffDays = diff;
			detailCSV.append(diffDays + " Days " + diffHours + "hrs");
			// detailCSV.append("Step Release Time");
		}

		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Step Release By
		if (this.getStepReleaseBy() != null)
			detailCSV.append(this.getStepReleaseBy());
		// detailCSV.append("Step Release By");
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		// Step name
		if (this.getStepNameCsv() != null)
			detailCSV.append(getStepNameCsv());
		detailCSV.append(ApprovePaymentsReportConstants.csvSeperator);

		detailCSV.append(ApprovePaymentsReportConstants.newLine);

		return detailCSV;
	}

	private String addQuotes(String val) {
		if (val != null)
			return "\"" + val + "\"";
		return val;
	}
	
	@Override
	public ICoverable clone() throws CloneNotSupportedException {
		return new ApprovePaymentWorkItem(this.getPayment(),this.isActive(), this.getUsername(), this.getDefinedApprovals());
	}
	
	/*public ApprovePaymentWorkItem(Payment payment, boolean isActive,
			String username, List<CaseApprovalStep> definedApprovals ) {
		paymentWorkItem = new PaymentWorkItem();
		this.setCcy(payment.getCurrency());
		this.setNominal(payment.getNominalAmount().doubleValue());
		this.setCaseNumber(payment.getCaseId());
		this.setClientId(payment.getMasterCaseRecord().getParentId());
		this.setFundingDeadline(payment.getFundingDeadline());
		this.setPaymentDeadline(payment.getPaymentDeadline());
		this.setValueDate(payment.getValueDate());
		this.setUsdEquivalent(payment.getUsdEquivalent().doubleValue());
		// for lock case
		log.info("Locked By---->>"+payment.getLockedCases().getLockedBy());
		 this.setLockedBy(payment.getLockedCases().getLockedBy());*/
}