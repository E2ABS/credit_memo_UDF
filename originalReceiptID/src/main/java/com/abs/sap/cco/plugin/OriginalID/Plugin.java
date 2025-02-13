package com.abs.sap.cco.plugin.OriginalID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.scco.ap.plugin.BasePlugin;
import com.sap.scco.ap.plugin.annotation.ListenToExit;
import com.sap.scco.ap.plugin.annotation.PluginAt;
import com.sap.scco.ap.plugin.annotation.PluginAt.POSITION;
import com.sap.scco.ap.pos.dao.IReceiptManager;


import generated.GenericValues;
import generated.PostInvoiceType;


import com.sap.scco.ap.pos.entity.ReceiptEntity;
import com.sap.scco.ap.pos.entity.SalesItemEntity;
import com.sap.scco.ap.pos.service.ReceiptPosService;





public class Plugin extends BasePlugin {

	private static final Logger logger = LoggerFactory.getLogger(Plugin.class);

	public final static String PLUGIN_ID = "OriginalID";
	
	private static String orgId = "";
	
	 ReceiptEntity receipt = new ReceiptEntity();
	
	public Map<String, String> documentCommentsMap = new HashMap<>();
	
	@Override
	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public String getName() {
		return "OriginalID";
	}
	@Override
	public boolean persistPropertiesToDB() {
		return true;
	}

	@Override
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}
	
	@Override
	public void startup() {
		super.startup();
		System.out.println("Hello world, this is Jamal's coding");
	}
	
	public Map<String, String> getPluginPropertyConfig() {
		Map<String, String> propertyConfig = new HashMap<>();
		
		propertyConfig.put("UDF", "String");
		
		return propertyConfig;
	}
	
	@PluginAt(pluginClass=ReceiptPosService.class, method = "postReceipt", where=POSITION.BEFORE)	
	public void beforeOtherModes(Object proxy, Object[] args, StackTraceElement callStack)
	{
		logger.info("beforeOtherModes started");
		ReceiptEntity R = (ReceiptEntity)args[0];
		
		List<SalesItemEntity> SalesItems = new ArrayList<>();
		SalesItems = R.getSalesItems();
		
		String str = SalesItems.get(0).getReferenceSalesItem().getReceipt().getId();
		
		logger.info("receipt id from plugin method: " + str);
		
		if(str != null) {
			orgId = str;
			R.setOriginalReceiptId(str);
			}
		
		logger.info("original ID = " + orgId);

		
	}
	
	
	@PluginAt(pluginClass=IReceiptManager.class, method = "finishReceipt", where=POSITION.BEFORE)	
	public void beforeRetailMode(Object proxy, Object[] args, StackTraceElement callStack)
	{
		logger.info("beforeRetailMode started *** ");
		ReceiptEntity R = (ReceiptEntity)args[0];
		
		List<SalesItemEntity> SalesItems = new ArrayList<>();
		SalesItems = R.getSalesItems();
		
		String str = SalesItems.get(0).getReferenceSalesItem().getReceipt().getId();
		
		logger.info("receipt id from plugin method: " + str);
		
		if(str != null) {
			orgId = str;
			R.setOriginalReceiptId(str);
			}
		
		logger.info("original ID = " + orgId);
		
		
			
	}

	
	@ListenToExit(exitName = "BusinessOneServiceWrapper.beforePostInvoiceRequest")
	public void beforPostInvoice(Object caller, Object[] args) {
	    PostInvoiceType request = (PostInvoiceType) args[1];
	    
	    logger.info("before post invoice entered");
	    
	    
	    try {
	        if (request == null) {
	            logger.error("Request object is null in beforPostInvoice");
	            return;
	        }
	        
	        if (request.getSale() == null) {
	            logger.error("Sale object is null in request");
	            return;
	        }
	        
	        if (request.getSale().getDocuments() == null) {
	            logger.error("Documents object is null in Sale");
	            return;
	        }
	        

	        // Setting up and validating GenericValues for Documents
	        GenericValues documentsGenericValues = request.getSale().getDocuments().getGenericValues();
	        if (documentsGenericValues == null) {
	            documentsGenericValues = new GenericValues();
	            request.getSale().getDocuments().setGenericValues(documentsGenericValues);
	        }


	        
	        logger.info("right before add val pair: " + orgId);
	        // Adding key-value pairs with null checks
	        addKeyValPair(documentsGenericValues, this.getProperty("UDF", "String"), orgId);
	        
	        args[1] = request;  // Updating reference in the array if necessary

	        logger.info("right before resetting value: " + orgId);
	        
	        orgId = "";
	        
	    } catch (NullPointerException e) {
	        logger.error("NullPointerException caught in beforPostInvoice", e);
	    } catch (Exception e) {
	        logger.error("Unexpected error in beforPostInvoice", e);
	    }
	}


	// Helper method to add KeyValPairs with a null check
				private void addKeyValPair(GenericValues genericValues, String key, String value) {
					
					logger.info("key value: " + key);
				    if (value != null) {
				        GenericValues.KeyValPair keyValPair = new GenericValues.KeyValPair();
				        keyValPair.setKey(key);
				        keyValPair.setValue(value);
				        genericValues.getKeyValPair().add(keyValPair);
				    } else {
				        logger.warn("Null value for key: " + key);
				    }
				}
	
	
	
	   
}