// File:         ReadPackingListImpl.java
// Created:      Mar 7, 2011
// Author:       KamalB
//
// This code is copyright (c) 2011 Lisi Aerospace
// 
// History:
//  
//
package com.gavs.lisi.webservices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.gavs.lisi.constants.M3Parameter;
import com.gavs.lisi.exception.M3WebserviceException;
import com.gavs.lisi.m3access.M3Client;
import com.gavs.lisi.m3access.WebServiceClient;
import com.gavs.lisi.model.Allocation;
import com.gavs.lisi.model.MailTrigger;
import com.gavs.lisi.model.Program;
import com.gavs.lisi.model.Result;
import com.gavs.lisi.util.M3APIParameter;
import com.gavs.lisi.util.TextUtil;

/**
 * The Class ReadPackingListImpl.
 */
@WebService(endpointInterface = "com.gavs.lisi.webservices.ReadPackingList")
public class ReadPackingListImpl extends BaseService implements ReadPackingList {

	private static final String EMPTY_STRING = "";
	private static final int MILLION = 1000000;
	private static final Logger logger = Logger
			.getLogger(ReadPackingListImpl.class);
	/** The web service client. */
	private WebServiceClient webServiceClient;

	// Begin WO# 27639 - Moving Static links from Application to Database -
	// Ambrish - Infosys - 21 June 2011

	/** The packinglist text util. */
	private TextUtil packinglistTextUtil;
	private MailTrigger mailTrigger;
	
	StringBuilder additionalRef = new StringBuilder();
	StringBuilder spec = new StringBuilder();
	StringBuilder aliasNumber = new StringBuilder();

	// End WO# 27639 - Moving Static links from Application to Database -
	// Ambrish - Infosys - 21 June 2011

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gavs.lisi.webservices.ReadPackingList#getPackingList(java.lang.String
	 * )
	 */
	@Override
	public Allocation getPackingList(String allocateID) {
		// TODO Auto-generated method stub
		Allocation allocation = null;
		String outputMessage = null;
		System.out.println("getPackingList");
		mailTrigger.setMailSend("true");
		System.out.println("MailSend=" + mailTrigger.getMailSend());
		try {

			allocation = getPickListDAO().getPackingListDetails(allocateID);

			allocation.setAllocateID(allocateID);
			HashMap<String, String> inputParameters = new HashMap<String, String>();

			// Begin WO# 27639 - Moving Static links from Application to
			// Database -
			// Ambrish - Infosys - 21 June 2011

			inputParameters.put(M3Parameter.CONO.getValue(), getM3Parameters()
					.getM3Company());

			// End WO# 27639 - Moving Static links from Application to Database
			// -
			// Ambrish - Infosys - 21 June 2011

			inputParameters.put(M3Parameter.DLIX.getValue(),
					allocation.getDeliveryNumber());

			String[] output = { M3Parameter.WHLO.getValue(),
					M3Parameter.ITNO.getValue(), M3Parameter.WHSL.getValue(),
					M3Parameter.BANO.getValue(), M3Parameter.RIDN.getValue(),
					M3Parameter.RIDL.getValue(), M3Parameter.ITDS.getValue(),
					M3Parameter.FUDS.getValue(), M3Parameter.CUNO.getValue(),
					M3Parameter.CUOR.getValue(), M3Parameter.CUPO.getValue(),
					M3Parameter.CUNM.getValue(), M3Parameter.ORQT.getValue(),
					M3Parameter.RNQT.getValue(), M3Parameter.ADID.getValue(),
					M3Parameter.MODL.getValue(), M3Parameter.SMCD.getValue(),
					M3Parameter.CUA1.getValue(), M3Parameter.CUA2.getValue(),
					M3Parameter.CUA3.getValue(), M3Parameter.CSCD.getValue(),
					M3Parameter.ECAR.getValue(), M3Parameter.PONO.getValue(),
					M3Parameter.TOWN.getValue(), M3Parameter.CUA5.getValue(),
					M3Parameter.CUA6.getValue(), M3Parameter.CUA7.getValue(),
					M3Parameter.CSCD1.getValue(), M3Parameter.ECAR1.getValue(),
					M3Parameter.PONO1.getValue(), M3Parameter.TOWN1.getValue(),
					M3Parameter.ORTY.getValue(), M3Parameter.STQT.getValue(),
					M3Parameter.CUNM1.getValue(), M3Parameter.DWDT.getValue(),
					M3Parameter.POTX.getValue(), M3Parameter.POTL.getValue(),
					M3Parameter.SAPR.getValue(), M3Parameter.TEDL.getValue(),
					M3Parameter.CRAM.getValue(), M3Parameter.ORST.getValue(),
					M3Parameter.CONN.getValue(), M3Parameter.ETRN.getValue(),
					M3Parameter.POPN.getValue(), M3Parameter.TRQT.getValue(),
					M3Parameter.REFE.getValue(), M3Parameter.DIM1.getValue(),
					M3Parameter.DIM2.getValue(), M3Parameter.DIM3.getValue(),
					M3Parameter.CFI1.getValue(), M3Parameter.CUA3.getValue(),
					M3Parameter.CUA4.getValue(), M3Parameter.CUA7.getValue(),
					M3Parameter.CUA8.getValue(), M3Parameter.PLDT.getValue(),
					M3Parameter.SAPR.getValue(), M3Parameter.ALWT.getValue(),
					M3Parameter.OTDS.getValue() };

			List<List> allocationList = (List<List>) getM3APIAdapter().getList(
					"DRZ100MI ", "LstShipments", inputParameters, output);
			System.out.println("Size of allocationList::"
					+ allocationList.size());

			List<Allocation> allocateList = new ArrayList<Allocation>();
			for (List<Result> resultList : allocationList) {
				Allocation allocate = new Allocation();
				for (Result result : resultList) {
					setInAllocationObject(result, allocate);
				}
				allocateList.add(allocate);
			}
			System.out.println("Size of allocateList::" + allocateList.size());
			for (Allocation m3allocate : allocateList) {

				if (m3allocate.getCoNumber().equals(allocation.getCoNumber())
						&& m3allocate.getLineNumber().equals(
								allocation.getLineNumber())
						&& m3allocate.getLotNumber().equals(
								removePrefixZero(allocation.getLotNumber()))) {
					System.out
							.println("CO number::LotNumber::Location Matches");
					m3allocate.setTotalPieces(allocation.getTotalPieces());
					m3allocate.setTotalBoxes(allocation.getTotalBoxes());
					m3allocate.setGrossWeight(allocation.getGrossWeight());
					m3allocate.setNetWeight(allocation.getNetWeight());
					m3allocate.setAllocateID(allocation.getAllocateID());
					m3allocate
							.setDeliveryNumber(allocation.getDeliveryNumber());
					m3allocate.setAllocatedQty(allocation.getAllocatedQty());
					m3allocate.setCarrierName(allocation.getCarrierName());
					m3allocate.setLotQuantity(allocation.getLotQuantity());
					m3allocate.setAddressNumber(allocation.getAddressNumber());
					m3allocate.setDivision(allocation.getDivision());
					m3allocate.setDimension(allocation.getDimension());
					m3allocate.setLotNumber(allocation.getLotNumber());

					fillDualCert(m3allocate);

					allocation = m3allocate;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Get delivery specification for the Boeing LB customer.
		if (allocation != null) {
			getDeliverySpecification(allocation);
			// Share point 1781
			getLotReference(allocation);
			// START - WO 28159 - Print cross references On Picklist and Packing
			// List
			// Get cross reference
			getCrossReference(allocation);
			// END - WO 28159 - Print cross references On Picklist and Packing
			// List
			// START - WO 28065 - Print TEDS On Picklist and Packing List
			getOrderDetailsForPackingList(allocation);
			// END - WO 28065 - Print TEDS On Picklist and Packing List

			// getting TSO statement
			if (StringUtils.equals("HSC", allocation.getDivision())
					|| StringUtils.equals("MDK", allocation.getDivision())) {
				getTSOApproval(allocation);
			}
			System.out.println("Order type Value :"
					+ allocation.getOrderType());
			System.out.println("Division  Value:"
					+ allocation.getDivision());
			System.out.println("Customer ID Value:"
					+ allocation.getCustomerID());
			
			//Add Min-Max PartNumber
			// getAdditionalReferences(allocation);
			if (StringUtils.equals("ODI", allocation.getOrderType())
					&& StringUtils.equals("59123", allocation.getCustomerID())
					&& StringUtils.equals("HSC", allocation.getDivision())) {
				//getAdditionalReferences(allocation);
			}
			
		}

		// Get cross reference
		if (allocation != null) {
			getCrossReference(allocation);
		}

		packinglistTextUtil.setText(getM3APIAdapter(), allocation);

		System.out.println("allocation.getCustomerName() = "
				+ allocation.getCustomerName());
		System.out.println("CustItemNumber........."+allocation.getCustItemNumber());

		setAliasCategoryAndCatalogNumber(allocation);
		if(StringUtils.equals("ODI", allocation.getOrderType())){
			allocation.setOrderType("ODI");
		}else{
			allocation.setOrderType("ODI_Type");
		}

		return allocation;
	}

	private void getAdditionalReferences(Allocation allocation) {
		// TODO Auto-generated method stub

		HashMap<String, String> inputParameters = new HashMap<String, String>();
		inputParameters.put("Company", getM3Parameters().getM3Company());
		inputParameters.put("Warehouse", "WSH");
		inputParameters.put("ItemNumber", allocation.getItemNumber());
		List<Allocation> allocateList = null;
		M3APIParameter parameter = new M3APIParameter();
		parameter.setWebServiceName("MMZ002MI");
		parameter.setInputItem("LstMMXWHItem");
		parameter.setOutputItem("LstMMXWHResponseItem");
		parameter.setFunctionName("LstMMXWH");
		parameter.setReadOrWrite("read");
		parameter.setInputParameters(inputParameters);

		try {
			// Call the service to get the customer by passing the parameter

			allocateList = (ArrayList<Allocation>) new M3Client().callM3API(
					parameter, getM3Parameters());

		} catch (Exception e) {
			System.out
					.println("*********#######***************** in side catch m3api");
			e.printStackTrace();
		}

		System.out.println("Size of allocateList::" + allocateList.size());

		if (allocateList == null || allocateList.size() <= 0) {
			return;
		}
		
		ArrayList specList = new ArrayList();
		int i = 1;
		for (Allocation allocate : allocateList) {
			String flag = null;

			System.out.println("Customer Item Number :"
					+ allocation.getCustItemNumber());
			System.out.println("The Alias Number is :"
					+ allocate.getAliasNumber());
			System.out.println("The Flag value is :" + allocate.getFlag());
			/*
			 * if(!allocate.getAliasNumber().equals(allocation.getCustItemNumber(
			 * )) && allocate.getFlag().equals("1")){
			 * System.out.println("The spec value :"+allocate.getSpec());
			 * spec.append(allocate.getSpec().trim());
			 * if(allocate.getSpec().length()>0){ spec.append(", "); }
			 * aliasNumber.append(allocate.getAliasNumber().trim());
			 * if(allocate.getAliasNumber().length()>0){
			 * aliasNumber.append(", "); } }
			 */
			if (allocate.getAliasNumber()
					.equals(allocation.getCustItemNumber())) {
				System.out.println("if....The spec value :"
						+ allocate.getSpec());
				/*
				 * spec.append(allocate.getSpec().trim());
				 * if(allocate.getSpec().length()>0){ spec.append(", "); }
				 */

			} else {
				System.out.println("else.....The spec value :"
						+ allocate.getSpec());
				spec.append(allocate.getSpec().trim());
				if (allocate.getSpec().length() > 0) {
					spec.append(", ");
				}
			}
			if (!allocate.getAliasNumber().equals(
					allocation.getCustItemNumber())
					&& allocate.getFlag().equals("1")) {
				System.out.println("The Alias Number values :"
						+ allocate.getAliasNumber());
				aliasNumber.append(allocate.getAliasNumber().trim());
				if (allocate.getAliasNumber().length() > 0) {
					aliasNumber.append(", ");
				}
			}

			if (allocate.getAliasNumber()
					.equals(allocation.getCustItemNumber())
					&& allocate.getFlag().equals("1")) {
				System.out.println("The Additional References value is :"
						+ allocate.getAdditionalReference());
				additionalRef.append(allocate.getAdditionalReference());
				if (allocate.getAdditionalReference().length() > 0) {
					additionalRef.append(", ");
				}

			}
			System.out.println("The count is :" + i);
			i++;

		}
		if (additionalRef.length() > 0) {
			additionalRef.deleteCharAt(additionalRef.length() - 1);
			additionalRef.deleteCharAt(additionalRef.length() - 1);
		}
		allocation.setAdditionalReference(additionalRef.toString());
		System.out.println("Additional references values :"
				+ allocation.getAdditionalReference());

		if (spec.length() > 0) {
			spec.deleteCharAt(spec.length() - 1);
			spec.deleteCharAt(spec.length() - 1);
		}
		allocation.setSpec(spec.toString());
		System.out.println("the Spec values :" + allocation.getSpec());

		if (aliasNumber.length() > 0) {
			aliasNumber.deleteCharAt(aliasNumber.length() - 1);
			aliasNumber.deleteCharAt(aliasNumber.length() - 1);
		}
		allocation.setAliasNumber(aliasNumber.toString());
		System.out.println("The alias number values :"
				+ allocation.getAliasNumber());

	}

	private void getTSOApproval(Allocation allocation) {
		// TODO Auto-generated method stub

		// MMZ002MIGet Webservice
		HashMap<String, String> inputParameters = new HashMap<String, String>();
		inputParameters.put("Company", getM3Parameters().getM3Company());
		inputParameters.put("Warehouse", allocation.getWareHouse());
		inputParameters.put("ItemNumber", allocation.getItemNumber());
		List<Allocation> allocateList = null;
		M3APIParameter parameter = new M3APIParameter();
		parameter.setWebServiceName("MMZ200MIGet");
		parameter.setInputItem("GetItmWhsSpecItem");
		parameter.setOutputItem("GetItmWhsSpecResponseItem");
		parameter.setFunctionName("GetItmWhsSpec");
		parameter.setReadOrWrite("read");
		parameter.setInputParameters(inputParameters);

		try {
			// Call the service to get the customer by passing the parameter

			allocateList = (ArrayList<Allocation>) new M3Client().callM3API(
					parameter, getM3Parameters());

		} catch (Exception e) {
			System.out
					.println("*********#######***************** in side catch m3api");
			e.printStackTrace();
		}

		System.out.println("Size of allocateList::" + allocateList.size());

		for (Allocation m3allocate : allocateList) {
			System.out.println("M3 Allocate TSO Approval Value :"
					+ m3allocate.getTsoApproval());
			allocation.setTsoApproval(m3allocate.getTsoApproval());
			System.out.println("Allocation TSO Approval Value :"
					+ allocation.getTsoApproval());
		}

	}

	private void parsePONumber(Allocation m3allocate, Allocation dbAllocate) {
		String customerPONumber = m3allocate.getCustomerPONumber();
		if (StringUtils.isNotEmpty(customerPONumber)) {
			splitPONumberString(m3allocate, customerPONumber);
			return;
		}
		customerPONumber = dbAllocate.getCustomerPONumber();
		if (StringUtils.isNotEmpty(customerPONumber)) {
			splitPONumberString(m3allocate, customerPONumber);
			return;
		}
	}

	private void splitPONumberString(Allocation m3allocate,
			String customerPONumber) {
		String[] poAndLine = customerPONumber.split(";");
		m3allocate.setCustomerPONumber(poAndLine[0]);
		m3allocate.setCustomerPOLine(poAndLine[1]);
	}

	/**
	 * Gets the lot reference.
	 * 
	 * @param allocation
	 *            the allocation
	 * @return the lot reference
	 */
	private void getLotReference(Allocation allocation) {
		Program mainProgram = new Program();

		HashMap<String, String> map = new HashMap<String, String>();

		map.put(M3Parameter.BANO.getValue(), allocation.getLotNumber());
		map.put(M3Parameter.ITNO.getValue(), allocation.getItemNumber());

		mainProgram.setInputData(map);
		mainProgram.setWebServiceName("MMS235Read");
		mainProgram.setM3Function("MMS235");
		mainProgram.setFunctionName("getLotDetails");

		String lotReference = null;
		try {
			lotReference = (String) getWebServiceClient().callDisplayProgram(
					mainProgram);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		if (lotReference != null) {
			allocation.setLotReference(lotReference);
		}
	}

	/**
	 * Sets the in allocation object.
	 * 
	 * @param result
	 *            the result
	 * @param allocation
	 *            the allocation
	 */
	public void setInAllocationObject(Result result, Allocation allocation) {
		M3Parameter parameter = M3Parameter.fromString(result.getKey());
		long qty;
		switch (parameter) {
		case BANO:
			allocation.setLotNumber(removePrefixZero(result.getValue().trim()));
			break;
		case RIDL:
			allocation
					.setLineNumber(removePrefixZero(result.getValue().trim()));
			break;
		case RIDN:
			allocation.setCoNumber(result.getValue().trim());
			break;
		case CUNO:
			allocation.setCustomerID(result.getValue().trim());
			break;
		case ITNO:
			allocation
					.setItemNumber(removePrefixZero(result.getValue().trim()));
			break;
		case ORQT:
			qty = Long.parseLong(result.getValue().trim()) / MILLION;

			allocation.setOrderQty(String.valueOf(qty));
			break;
		case RNQT:
			qty = Long.parseLong(result.getValue().trim()) / MILLION;
			allocation.setRemainingQty(String.valueOf(qty));
			break;
		case CUOR:
			allocation.setCustomerPONumber(result.getValue().trim());
			break;
		case DWDT:
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat requiredFormat = new SimpleDateFormat("MM/dd/yyyy");
			String date = null;
			try {
				date = requiredFormat.format(format.parse(result.getValue()
						.trim()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			allocation.setRequestDate(date);
			break;
		case MODL:
			allocation.setShipMode(removePrefixZero(result.getValue().trim()));
			break;
		case STQT:
			qty = Long.parseLong(result.getValue().trim()) / MILLION;
			allocation.setLotQuantity(String.valueOf(qty));
			break;
		case SMCD:
			allocation
					.setSalesPerson(removePrefixZero(result.getValue().trim()));
			// } else if (result.getKey().equals("CUNM")) {
			// System.out.println("CUNMCUNMCUNMCUNMCUNMCUNM = "
			// + result.getValue().trim());
			// allocation.setCustomerName(removePrefixZero(result.getValue()
			// .trim()));
			// System.out.println("allocation.getCustomerName() = "
			// + allocation.getCustomerName());
			// } else if (result.getKey().equals("CUNM1")) {
			// allocation.setShipToDeliveryLocationName(removePrefixZero(result
			// .getValue().trim()));
			// } else if (result.getKey().equals("ADID")) {
			break;
		case CUNM1:
			System.out.println("CUNMCUNMCUNMCUNMCUNMCUNM = "
					+ result.getValue().trim());
			allocation.setCustomerName(removePrefixZero(result.getValue()
					.trim()));
			System.out.println("allocation.getCustomerName() = "
					+ allocation.getCustomerName());
			break;
		case CUNM:
			allocation.setShipToDeliveryLocationName(removePrefixZero(result
					.getValue().trim()));
			break;
		case ADID:
			allocation.setAddressNumber(removePrefixZero(result.getValue()
					.trim()));
			allocation.setShipToDeliveryLocationID((result.getValue().trim()));
			break;
		case CUA5:
			allocation.setCustomerAddress1(removePrefixZero(result.getValue()
					.trim()));
			break;
		case CUA6:
			allocation.setCustomerAddress2(removePrefixZero(result.getValue()
					.trim()));
			break;
		case CUA7:
			allocation.setCustomerAddress3(removePrefixZero(result.getValue()
					.trim()));
			break;
		case CUA8:
			allocation.setCustomerAddress4(removePrefixZero(result.getValue()
					.trim()));
			break;
		case TOWN1:
			allocation.setCustomerCity(removePrefixZero(result.getValue()
					.trim()));
			break;
		case ECAR1:
			allocation.setCustomerState(removePrefixZero(result.getValue()
					.trim()));
			break;
		case PONO1:
			allocation
					.setCustomerZip(removePrefixZero(result.getValue().trim()));
			break;
		case CSCD1:
			allocation.setCustomerCountry(removePrefixZero(result.getValue()
					.trim()));
			break;
		case ITDS:
			allocation.setItemDescription(removePrefixZero(result.getValue()
					.trim()));
			break;
		case FUDS:
			allocation
					.setDescription(removePrefixZero(result.getValue().trim()));
			break;
		case CUA1:
			allocation
					.setShipToDeliveryLocationAddress1(removePrefixZero(result
							.getValue().trim()));
			break;
		case CUA2:
			allocation
					.setShipToDeliveryLocationAddress2(removePrefixZero(result
							.getValue().trim()));
			break;
		case CUA3:
			allocation
					.setShipToDeliveryLocationAddress3(removePrefixZero(result
							.getValue().trim()));
			break;
		case CUA4:
			allocation
					.setShipToDeliveryLocationAddress4(removePrefixZero(result
							.getValue().trim()));
			break;
		case TOWN:
			allocation.setShipToDeliveryLocationCity(removePrefixZero(result
					.getValue().trim()));
			break;
		case ECAR:
			allocation.setShipToDeliveryLocationState(removePrefixZero(result
					.getValue().trim()));
			break;
		case PONO:
			allocation.setShipToDeliveryLocationZip((result.getValue().trim()));
			break;
		case CSCD:
			allocation.setShipToDeliveryLocationCountry(removePrefixZero(result
					.getValue().trim()));
			break;
		case WHSL:
			allocation.setLocation(removePrefixZero(result.getValue().trim()));
			break;
		case WHLO:
			allocation.setWareHouse(removePrefixZero(result.getValue().trim()));
			break;
		case CUPO:
			allocation.setCustomerPOLine(removePrefixZero(result.getValue()
					.trim()));
			break;
		case POPN:
			/*allocation.setCustItemNumber(removePrefixZero(result.getValue()
					.trim()));*/
			allocation.setCustItemNumber(result.getValue()
					.trim());
			break;
		case CONN:
			allocation.setShipmentNumber(removePrefixZero(result.getValue()
					.trim()));
			break;
		case TEDL:
			allocation.setDeliveryTerms(removePrefixZero(result.getValue()
					.trim()));
			break;
		case TRQT:
			qty = Long.parseLong(result.getValue().trim()) / MILLION;
			allocation.setShippedQty(String.valueOf(qty));
			break;
		case ORTY:
			allocation.setOrderType(result.getValue().trim());
			break;
		case REFE:
			allocation.setReference(result.getValue().trim());
			break;
		case DIM1:
			setSpec(allocation, result);
			break;
		case DIM2:
			setSpec(allocation, result);
			break;
		case DIM3:
			setSpec(allocation, result);
			break;
		case CFI1:
			if (result.getValue() != null) {
				allocation.setExportControl(result.getValue().trim());
			}
			break;
		case IVQT:
			qty = Long.parseLong(result.getValue().trim()) / MILLION;
			allocation.setInvoiceQuantity(String.valueOf(qty));
			break;
		case PLQT:
			qty = Long.parseLong(result.getValue().trim()) / MILLION;
			allocation.setPicklistQuantity(String.valueOf(qty));
			break;
		case DLQT:
			qty = Long.parseLong(result.getValue().trim()) / MILLION;
			allocation.setDeliveredQuantity(String.valueOf(qty));
			break;
		case POTX:
			if (result.getValue() != null) {
				allocation.setHeaderPostTextID(result.getValue().trim());
			}
			break;
		case POTL:
			if (result.getValue() != null) {
				allocation.setLinePostTextID(result.getValue().trim());
			}
			break;
		case SAPR:
			if (StringUtils.trimToNull(result.getValue()) != null) {
				allocation.setSalesPrice(Double.parseDouble(result.getValue()
						.trim()));
			}
			break;
		case PLDT:
			SimpleDateFormat formatPLDate = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat requiredFormatPLDate = new SimpleDateFormat(
					"MM/dd/yyyy");
			String plDate = null;
			try {
				plDate = requiredFormatPLDate.format(formatPLDate.parse(result
						.getValue().trim()));
			} catch (ParseException e) {
				logger.error(e.getMessage());
			}
			allocation.setPlanningDate(plDate);
			break;
		case ALWT:
			int aliasCategory = 0;
			if (StringUtils.trimToNull(result.getValue()) != null) {
				aliasCategory = Integer.parseInt(result.getValue().trim());
			}
			allocation.setAliasCategory(aliasCategory);
			break;
		case OTDS:
			allocation.setOrderItemDescription(removePrefixZero(result
					.getValue().trim()));
			break;
		default:
			break;
		}

		allocation.setPickListID(EMPTY_STRING);
		allocation.setDiameter(EMPTY_STRING);
		allocation.setTextLine1(EMPTY_STRING);
		allocation.setTextLine2(EMPTY_STRING);
		allocation.setTextLine3(EMPTY_STRING);
		allocation.setTextLine4(EMPTY_STRING);
		allocation.setItemUM(EMPTY_STRING);
		allocation.setNetWeight(EMPTY_STRING);
		allocation.setTermsDiscountPercent1(EMPTY_STRING);
		allocation.setTermsDiscountDays1(EMPTY_STRING);
		allocation.setTermsDiscountPercent2(EMPTY_STRING);
		allocation.setTermsDiscountDays2(EMPTY_STRING);
		allocation.setTermsDiscountNetDays(EMPTY_STRING);
	}

	/**
	 * Removes the prefix zero.
	 * 
	 * @param value
	 *            the value
	 * @return the string
	 */
	public String removePrefixZero(String value) {
		while (value.startsWith("0")) {
			value = value.substring(value.indexOf("0") + 1, value.length());
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gavs.lisi.webservices.ReadPackingList#getPrePackingList(java.lang
	 * .String)
	 */
	@Override
	public Allocation getPrePackingList(String allocateID) {

		Allocation allocation = null;
		String outputMessage = null;
		try {
			allocation = getPickListDAO().getPrePackingListDetails(allocateID);
			allocation.setAllocateID(allocateID);
			HashMap<String, String> inputParameters = new HashMap<String, String>();
			inputParameters.put(M3Parameter.BANO.getValue(),
					allocation.getLotNumber());

			// Begin WO# 27639 - Moving Static links from Application to
			// Database -
			// Ambrish - Infosys - 21 June 2011

			inputParameters.put(M3Parameter.CONO.getValue(), getM3Parameters()
					.getM3Company());

			// End WO# 27639 - Moving Static links from Application to Database
			// -
			// Ambrish - Infosys - 21 June 2011

			inputParameters.put(M3Parameter.PLSX.getValue(),
					allocation.getPickListSuffix());
			inputParameters.put(M3Parameter.RIDI.getValue(),
					allocation.getDeliveryNumber());
			inputParameters.put(M3Parameter.STAT.getValue(), "40");
			inputParameters.put(M3Parameter.WHLO.getValue(),
					allocation.getWareHouse());

			String[] output = { M3Parameter.BANO.getValue(),
					M3Parameter.CONO.getValue(), M3Parameter.CRAM.getValue(),
					M3Parameter.CSCD.getValue(), M3Parameter.CSCD1.getValue(),
					M3Parameter.FUDS.getValue(), M3Parameter.ITDS.getValue(),
					M3Parameter.ITNO.getValue(), M3Parameter.CUNO.getValue(),
					M3Parameter.CUOR.getValue(), M3Parameter.CUA1.getValue(),
					M3Parameter.CUA2.getValue(), M3Parameter.CUA3.getValue(),
					M3Parameter.CUA5.getValue(), M3Parameter.CUA6.getValue(),
					M3Parameter.CUA7.getValue(), M3Parameter.CUNM.getValue(),
					M3Parameter.CUNM1.getValue(), M3Parameter.ADID.getValue(),
					M3Parameter.CONN.getValue(), M3Parameter.CUPO.getValue(),
					M3Parameter.DWDT.getValue(), M3Parameter.ECAR.getValue(),
					M3Parameter.ECAR1.getValue(), M3Parameter.ETRN.getValue(),
					M3Parameter.MODL.getValue(), M3Parameter.ORQT.getValue(),
					M3Parameter.ORST.getValue(), M3Parameter.PONO.getValue(),
					M3Parameter.PONO1.getValue(), M3Parameter.POPN.getValue(),
					M3Parameter.POTL.getValue(), M3Parameter.POTX.getValue(),
					M3Parameter.RIDL.getValue(), M3Parameter.RIDN.getValue(),
					M3Parameter.RNQT.getValue(), M3Parameter.SAPR.getValue(),
					M3Parameter.SMCD.getValue(), M3Parameter.STQT.getValue(),
					M3Parameter.TEDL.getValue(), M3Parameter.TOWN.getValue(),
					M3Parameter.TOWN1.getValue(), M3Parameter.WHLO.getValue(),
					M3Parameter.WHSL.getValue(), M3Parameter.REFE.getValue(),
					M3Parameter.DIM1.getValue(), M3Parameter.DIM2.getValue(),
					M3Parameter.DIM3.getValue(), M3Parameter.CFI1.getValue(),
					M3Parameter.IVQT.getValue(), M3Parameter.PLQT.getValue(),
					M3Parameter.DLQT.getValue(), M3Parameter.CUA3.getValue(),
					M3Parameter.CUA4.getValue(), M3Parameter.CUA7.getValue(),
					M3Parameter.CUA8.getValue(), M3Parameter.PLDT.getValue(),
					M3Parameter.SAPR.getValue(), M3Parameter.ALWT.getValue(),
					M3Parameter.OTDS.getValue(), M3Parameter.ORTY.getValue() };
			List<List> allocationList = (List<List>) getM3APIAdapter().getList(
					"MWZ420MI ", "LstPicklistData", inputParameters, output);

			List<Allocation> allocateList = new ArrayList<Allocation>();
			for (List<Result> resultList : allocationList) {
				Allocation allocate = new Allocation();
				for (Result result : resultList) {
					setInAllocationObject(result, allocate);
				}
				allocateList.add(allocate);
			}
			System.out.println("Size of allocateList::" + allocateList.size());
			for (Allocation m3allocate : allocateList) {
				System.out.println(m3allocate.getCoNumber() + "::"
						+ m3allocate.getLineNumber() + "::"
						+ m3allocate.getLotNumber() + "::"
						+ m3allocate.getLocation());
				System.out.println(allocation.getCoNumber() + "::"
						+ allocation.getLineNumber() + "::"
						+ allocation.getLotNumber() + "::"
						+ allocation.getLocation());
				if (m3allocate.getCoNumber().equals(allocation.getCoNumber())
						&& m3allocate.getLineNumber().equals(
								allocation.getLineNumber())
						&& m3allocate.getLotNumber().equals(
								removePrefixZero(allocation.getLotNumber()))
						&& m3allocate.getLocation().equals(
								removePrefixZero(allocation.getLocation()))) {

					System.out
							.println("CO #::Line#::Location::Lot#::Delivery#:: Matches");
					m3allocate.setAllocateID(allocation.getAllocateID());
					m3allocate.setAllocatedQty(allocation.getAllocatedQty());
					m3allocate
							.setDeliveryNumber(allocation.getDeliveryNumber());
					m3allocate.setAddressNumber(allocation.getAddressNumber());
					m3allocate
							.setShipmentNumber(allocation.getShipmentNumber());
					m3allocate.setShippedQty(allocation.getShippedQty());
					m3allocate.setGrossWeight(allocation.getGrossWeight());
					m3allocate.setNetWeight(allocation.getNetWeight());
					m3allocate.setTotalBoxes(allocation.getTotalBoxes());
					m3allocate
							.setCustomerPOLine(allocation.getCustomerPOLine());
					m3allocate.setCustomerID(allocation.getCustomerID());
					m3allocate.setDivision(allocation.getDivision());
					m3allocate.setCarrierName(allocation.getCarrierName());
					m3allocate.setDimension(allocation.getDimension());
					m3allocate.setLotNumber(allocation.getLotNumber());
					// parsePONumber(m3allocate, allocation);

					fillDualCert(m3allocate);

					allocation = m3allocate;
					
				}
			}
			// allocation.setCustItemNumber("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		packinglistTextUtil.setText(getM3APIAdapter(), allocation);
		System.out.println("CustItemNumber........."+allocation.getCustItemNumber());

		// Share point 1661 1662
		setAliasCategoryAndCatalogNumber(allocation);

		// Share point 1781
		if (allocation != null) {
			getLotReference(allocation);
			// START - WO 28159 - Print cross references On Picklist and Packing
			// List
			// Get cross reference
			getCrossReference(allocation);
			// END - WO 28159 - Print cross references On Picklist and Packing
			// List
			// START - WO 28065 - Print TEDS On Picklist and Packing List
			getOrderDetailsForPrePackingList(allocation);
			// END - WO 28065 - Print TEDS On Picklist and Packing List

			// getting TSO statement
			if (StringUtils.equals("HSC", allocation.getDivision())
					|| StringUtils.equals("MDK", allocation.getDivision())) {
				getTSOApproval(allocation);
			}
			System.out.println("Order type Value :"
					+ allocation.getOrderType());
			System.out.println("Division  Value:"
					+ allocation.getDivision());
			System.out.println("Customer ID Value:"
					+ allocation.getCustomerID());
			//Add Min-Max PartNumber
			// getAdditionalReferences(allocation);
			if (StringUtils.equals("ODI", allocation.getOrderType())
					&& StringUtils.equals("59123", allocation.getCustomerID())
					&& StringUtils.equals("HSC", allocation.getDivision())) {
				//getAdditionalReferences(allocation);
			}
			
		}
		if(StringUtils.equals("ODI", allocation.getOrderType())){
			allocation.setOrderType("ODI");
		}else{
			allocation.setOrderType("ODI_Type");
		}
		
		return allocation;

	}

	private void fillDualCert(Allocation m3allocate) {
		if (StringUtils.equals("ODI", m3allocate.getOrderType())) {
			System.out.println("fillDualCert method ordertype is ODI Skip from here");
			return;
		}
		Map<String, String> dualCertInfo = getM3APIAdapter().getDualCert(
				getM3Parameters().getM3Company(), m3allocate.getCoNumber(),
				m3allocate.getLineNumber());
		System.out.println("fillDualCert Method dualCertInfo value:"+dualCertInfo);
		System.out.println("fillDualCertInfo ZDUC Value:"+dualCertInfo
				.get(M3Parameter.ZDUC.getValue()));
		if ("0".equals(StringUtils.trimToEmpty(dualCertInfo
				.get(M3Parameter.ZDUC.getValue())))) {
			System.out.println("fillDualCert method dualCertInfo value is 0 Skip from here");
			return;
		}
		m3allocate
				.setDcPartNumber(dualCertInfo.get(M3Parameter.POPN.getValue()));
		m3allocate.setDcTo(dualCertInfo.get(M3Parameter.DIM1.getValue()));
		System.out.println("fillDualCert() method DcPartNumber :"+m3allocate.getDcPartNumber());
		System.out.println("fillDualCert() method DcTO :"+m3allocate.getDcTo());
	}

	// Share point 1661 1662
	/**
	 * Sets the alias category and catalog number.
	 * 
	 * @param allocation
	 *            the new alias category and catalog number
	 */
	private void setAliasCategoryAndCatalogNumber(Allocation allocation) {
		if (allocation.getAliasCategory() == 1) {
			allocation.setCatalogNumber(EMPTY_STRING);
		} else if (allocation.getAliasCategory() == 6) {
			allocation.setCatalogNumber(allocation.getCustItemNumber());
			allocation.setCustItemNumber(allocation.getOrderItemDescription());
		} else {
			// Distribution order does not have any alias category
			allocation.setCatalogNumber(allocation.getCustItemNumber());
			allocation.setCustItemNumber(allocation.getItemDescription());
		}

	}

	/**
	 * Sets the web service client.
	 * 
	 * @param webServiceClient
	 *            the new web service client
	 */
	public void setWebServiceClient(WebServiceClient webServiceClient) {
		this.webServiceClient = webServiceClient;
	}

	/**
	 * Gets the web service client.
	 * 
	 * @return the web service client
	 */
	public WebServiceClient getWebServiceClient() {
		return webServiceClient;
	}

	// Begin WO# 27639 - Moving Static links from Application to Database -
	// Ambrish - Infosys - 21 June 2011

	/**
	 * @return the packinglistTextUtil
	 */
	public TextUtil getPackinglistTextUtil() {
		return packinglistTextUtil;
	}

	/**
	 * @param packinglistTextUtil
	 *            the packinglistTextUtil to set
	 */
	public void setPackinglistTextUtil(TextUtil packinglistTextUtil) {
		this.packinglistTextUtil = packinglistTextUtil;
	}

	public void setMailTrigger(MailTrigger mailTrigger) {
		this.mailTrigger = mailTrigger;
	}

	public MailTrigger getMailTrigger() {
		return mailTrigger;
	}
	// End WO# 27639 - Moving Static links from Application to Database -
	// Ambrish - Infosys - 21 June 2011

}
