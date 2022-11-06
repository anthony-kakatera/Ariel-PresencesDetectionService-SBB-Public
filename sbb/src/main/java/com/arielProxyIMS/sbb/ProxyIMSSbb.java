package com.arielProxyIMS.sbb;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sip.ClientTransaction;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderAddress;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.slee.*;
import javax.slee.facilities.TraceLevel;
import javax.slee.facilities.Tracer;

import net.java.slee.resource.sip.DialogActivity;
import net.java.slee.resource.sip.SipActivityContextInterfaceFactory;
import net.java.slee.resource.sip.SleeSipProvider;

import org.mobicents.slee.*;

import com.arielRegistrationService.sbb.RegistrationServiceSbbLocalObject;

import entities.Registrationbinding;
import sessionBeans.RegistrationbindingFacadeLocal;



public abstract class ProxyIMSSbb implements Sbb, ProxyIMS {
	private Tracer tracer;
	private Context sbbEnv;
	private SleeSipProvider provider;
	private MessageFactory messageFactory;
	private AddressFactory addressFactory;
	@SuppressWarnings("unused")
	private HeaderFactory headerFactory;
	private SipActivityContextInterfaceFactory sipActivityContextInterfaceFactory;
	private Context ejbContext;
	private RegistrationbindingFacadeLocal locationService;
	
	
	public void onREGISTER(javax.sip.RequestEvent event, ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		try {
			RegistrationServiceSbbLocalObject registrarSbbLocalObject = ((RegistrationServiceSbbLocalObject)getRegistrationServiceSbb().create());
			aci.attach(registrarSbbLocalObject);
		} catch (Exception e) {
			// failed to attach the register, send error back
			tracer.info(e.getMessage());
			// TODO send 500 back
		}
		// detach myself
		aci.detach(sbbContext.getSbbLocalObject());
	}

	public void on2XXRESPONSE(javax.sip.ResponseEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("2XX Response Received");
		processResponse(event, aci);
	}


	public void on1XXRESPONSE(javax.sip.ResponseEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("1XX Response Received");
		processResponse(event, aci);
	}

	public void on3XXRESPONSE(javax.sip.ResponseEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("3XX Response Received");
		processResponse(event, aci);
	}

	public void on4XXRESPONSE(javax.sip.ResponseEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("4XX Response Received");
		processResponse(event, aci);
	}

	public void on5XXRESPONSE(javax.sip.ResponseEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("5XX Response Received");
		processResponse(event, aci);
	}

	public void on6XXRESPONSE(javax.sip.ResponseEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("6XX Response Received");
		processResponse(event, aci);

	}

	public void onTRANSACTIONTIMEOUT(javax.sip.TimeoutEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
	
	}

	public void onSUBSCRIBE(javax.sip.RequestEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("Subscribe event received");
		processRequest(event.getServerTransaction(), event.getRequest(), aci);
	}

	// TODO: Perform further operations if required in these methods.
	public void setSbbContext(SbbContext context) { 
		this.sbbContext = (SbbContextExt) context;
		this.tracer = context.getTracer(ProxyIMSSbb.class.getSimpleName());
		
		try{
			sbbEnv = (Context)new InitialContext().lookup("java:comp/env");
			
			provider = (SleeSipProvider)sbbEnv.lookup("slee/resources/jainsip/1.2/provider");
			
			messageFactory = provider.getMessageFactory();
			
			headerFactory = provider.getHeaderFactory();
			
			addressFactory = provider.getAddressFactory();
			
			tracer.info("UNIQUE SIP setSbbContext(...)");
			
			sipActivityContextInterfaceFactory = (SipActivityContextInterfaceFactory) sbbEnv.lookup("slee/resources/jainsip/1.2/acifactory");
			
			ejbContext = new InitialContext();
			locationService = (RegistrationbindingFacadeLocal) ejbContext.lookup("RegistrationbindingFacade/local");
			
			
		}catch(NamingException ex){
			ex.printStackTrace();
		}  
	}
	public void unsetSbbContext() { this.sbbContext = null; }

	// TODO: Implement the lifecycle methods if required
	public void sbbCreate() throws javax.slee.CreateException {}
	public void sbbPostCreate() throws javax.slee.CreateException {}
	public void sbbActivate() {}
	public void sbbPassivate() {}
	public void sbbRemove() {}
	public void sbbLoad() {}
	public void sbbStore() {}
	public void sbbExceptionThrown(Exception exception, Object event, ActivityContextInterface activity) {}
	public void sbbRolledBack(RolledBackContext context) {}
	
	public abstract ChildRelationExt getRegistrationServiceSbb();
	public abstract com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface asSbbActivityContextInterface(ActivityContextInterface aci);


	
	/**
	 * Convenience method to retrieve the SbbContext object stored in setSbbContext.
	 * 
	 * TODO: If your SBB doesn't require the SbbContext object you may remove this 
	 * method, the sbbContext variable and the variable assignment in setSbbContext().
	 *
	 * @return this SBB's SbbContext object
	 */
	
	protected SbbContextExt getSbbContext() {
		return sbbContext;
	}

	private SbbContextExt sbbContext; // This SBB's SbbContext
	

	//initial method(s)
	private void processRequest(ServerTransaction serverTransaction, Request request, ActivityContextInterface ac) {
		tracer.info("Processing Request");
        //create dialog
        DialogActivity incomingDialog = null;
        DialogActivity outgoingDialog = null;
        try {
        	incomingDialog = (DialogActivity) provider.getNewDialog(serverTransaction);
        	outgoingDialog = provider.getNewDialog(incomingDialog, true);
			// Obtain the dialog activity contexts and attach to them
	        ActivityContextInterface dialogACI = sipActivityContextInterfaceFactory.getActivityContextInterface(incomingDialog);
	        dialogACI.attach(sbbContext.getSbbLocalObject());
	        // Obtain the dialog activity contexts and attach to them
            ActivityContextInterface outgoingDialogACI = sipActivityContextInterfaceFactory.getActivityContextInterface(outgoingDialog);
            ActivityContextInterface incomingDialogACI = sipActivityContextInterfaceFactory.getActivityContextInterface(incomingDialog);
            incomingDialogACI.attach(sbbContext.getSbbLocalObject());
            outgoingDialogACI.attach(sbbContext.getSbbLocalObject());
	        //Record the dialog
	        setIncomingDialog(incomingDialogACI);
            setOutgoingDialog(outgoingDialogACI);
	        forwardRequest(serverTransaction, outgoingDialog, true);
		} catch (Exception e) {
			tracer.severe("Error during Dialog process : ", e);
		}
	}

	private void forwardRequest(ServerTransaction st, DialogActivity out, boolean initial) throws SipException {
        // Copies the request, setting the appropriate headers for the dialog.
        Request incomingRequest = st.getRequest();
        Request outgoingRequest = out.createRequest(incomingRequest);

        if (initial) {
            // On initial request only, check if the destination address is inside one of our domains
            URI requestURI = incomingRequest.getRequestURI();
           
            tracer.info(requestURI + " is in a local domain, lookup address in location service");
            URI registeredAddress = locationEjbTargets(requestURI);
            
            if (registeredAddress == null) {
                if (tracer.isTraceable(TraceLevel.FINE)) tracer.fine("no registered address found for " + requestURI);
                sendErrorResponse(st, Response.TEMPORARILY_UNAVAILABLE);
                return;
            }

            tracer.info("found registered address: " + registeredAddress);
            outgoingRequest.setRequestURI(registeredAddress);
            
            //save both registered addresses
            String tempAddress = getCanonicalAddress((HeaderAddress)outgoingRequest.getHeader(FromHeader.NAME));
            URI tempAddressURI = null;
			try {
				tempAddressURI = addressFactory.createURI(tempAddress);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            tracer.info("THE from address : " + tempAddress);
            setToURI(registeredAddress.toString());
            
            setFromURI(locationEjbTargets(tempAddressURI).toString());
        }
        tracer.info("forwarding request on dialog " + out + ":\n" + outgoingRequest);
        tracer.info("processRequest ");
        //processRequest(outgoingRequest);
        if (incomingRequest.getMethod().equals(Request.ACK) ) {
        	outgoingRequest.setRequestURI(allocateRequestURI(getCanonicalAddress((HeaderAddress)outgoingRequest.getHeader(ToHeader.NAME))));
        	out.sendAck(outgoingRequest);
        }else if(incomingRequest.getMethod().equals(Request.BYE)){
        	outgoingRequest.setRequestURI(allocateRequestURI(getCanonicalAddress((HeaderAddress)outgoingRequest.getHeader(ToHeader.NAME))));
        	ClientTransaction ct = out.sendRequest(outgoingRequest);
            out.associateServerTransaction(ct, st);
        }else if(incomingRequest.getMethod().equals(Request.NOTIFY)){
        	outgoingRequest.setRequestURI(allocateRequestURI(getCanonicalAddress((HeaderAddress)outgoingRequest.getHeader(ToHeader.NAME))));
        	ClientTransaction ct = out.sendRequest(outgoingRequest);
            out.associateServerTransaction(ct, st);
        }
        else {
            // Send the request on the dialog activity
            ClientTransaction ct = out.sendRequest(outgoingRequest);
            out.associateServerTransaction(ct, st);
        }
    }
	
	/**
     * A request was received on one of our dialogs. Forward it to the other dialog.
     */
    private void processMidDialogRequest(javax.sip.RequestEvent event, ActivityContextInterface dialogACI) {
        tracer.info("received mid-dialog request on dialog " + dialogACI.getActivity() + ":\n" + event.getRequest());
        try {
            // Find the dialog to forward the request on
            ActivityContextInterface peerACI = getPeerDialog(dialogACI);
            forwardRequest(event.getServerTransaction(), (DialogActivity)peerACI.getActivity(), false);
        } catch (SipException e) {
        	tracer.warning("failed to forward request", e);
            sendErrorResponse(event.getServerTransaction(), Response.SERVER_INTERNAL_ERROR);
        }
    }
	
    private void processResponse(javax.sip.ResponseEvent event, ActivityContextInterface aci) {
        Response response = event.getResponse();
        tracer.finest("received response on dialog " + aci.getActivity() + ":\n" + event.getResponse());
        try {
            // Find the dialog to forward the response on
            ActivityContextInterface peerACI = getPeerDialog(aci);
            forwardResponse((DialogActivity)aci.getActivity(), (DialogActivity)peerACI.getActivity(), event.getClientTransaction(), response);
        } catch (SipException e) {
        	tracer.warning("failed to forward response", e);
        }
    }
    
    private void forwardResponse(DialogActivity in, DialogActivity out, ClientTransaction ct, Response receivedResponse) throws SipException {
        // Find the original server transaction that this response should be forwarded on.
        ServerTransaction st = in.getAssociatedServerTransaction(ct); // could be null
        if (st == null) throw new SipException("could not find associated server transaction: \n " );
        // Copy the response across, setting the appropriate headers for the dialog
        Response outgoingResponse = out.createResponse(st, receivedResponse);
        tracer.info("forwarding response on dialog " + out + ":\n" + outgoingResponse);
        // Forward response upstream.
        try {
            st.sendResponse(outgoingResponse);
        } catch (javax.sip.InvalidArgumentException e) {
			tracer.severe("Invalid Response", e);
		}
    }
	//helper methods
	private void sendErrorResponse(ServerTransaction st, int statusCode) {
        try {
            Response response =messageFactory.createResponse(statusCode, st.getRequest());
            st.sendResponse(response);
        } catch (Exception e) {
        	tracer.warning("failed to send error response", e);
        }
    }
	
	public URI locationEjbTargets(URI uri){
		String addressOfRecord = uri.toString();
		URI targetAddress = null;
		
		try{
		String []parts = addressOfRecord.split(":");
		String sudoAddress = parts[1];
		
		sudoAddress = parts[0]+":" + parts[1];
		
		if(sudoAddress.toLowerCase().contains(";")){
			String []otherParts = sudoAddress.split(";");
			sudoAddress = otherParts[0];
		}
		

		addressOfRecord = sudoAddress;
		}catch (Exception e){
			tracer.info("Failed to split and correct", e);
		}
		
		
		try {
			List<Registrationbinding> bindings =  null;
			tracer.info("Address of record: " + addressOfRecord);
			bindings =  locationService.retrieveSingleBinding(addressOfRecord);
			tracer.info("bindings: " + bindings);
			if (bindings == null) {
				tracer.warning("User not found");
			}
			if (bindings.isEmpty()) {
				tracer.warning("User Not In DATABASE");
			}
			
			Iterator<Registrationbinding> it = bindings.iterator();
			@SuppressWarnings("unused")
			URI target = null;
			while (it.hasNext()) {
				Registrationbinding binding = (Registrationbinding) it.next();
				String contactAddress = binding.getContactAddress();
				try {
					targetAddress = (addressFactory.createURI(contactAddress));
					tracer.info("Target found: " + contactAddress);
					//quickLookup.put(addressOfRecord, targetAddress);
					//setContactAddress(contactAddress);
				} catch (ParseException e) {
					tracer.warning("Ignoring contact address "+contactAddress+" due to parse error",e);
				}
			}
			if (targetAddress == null) {
				tracer.warning("User temporarily unavailable");
			}
			return targetAddress;
		} catch (TransactionRequiredLocalException e) {
			// TODO Auto-generated catch block
			tracer.severe("Error caught", e);
		} catch (SLEEException e) {
			// TODO Auto-generated catch block
			tracer.severe("SLEE Exception", e);
		} 
		return targetAddress;
	}
	
	public void sendStatelessRequest(Request request) throws SipException {
		provider.sendRequest(request);
	}
	
	
	public void sendStatelessResponse(Response response) throws SipException {
		provider.sendResponse(response);
	}
	
	private String getCanonicalAddress(HeaderAddress header) {
		String addr = header.getAddress().getURI().toString();
		int index = addr.indexOf(':');
		index = addr.indexOf(':', index + 1);
		if (index != -1) {
			// Get rid of the port
			addr = addr.substring(0, index);
		}
		return addr;
	}
	
	public ServerTransaction getServerTransaction(ClientTransaction clientTransaction) {
		ActivityContextInterface myacis[] = sbbContext.getActivities();
		for (int i = 0; i < myacis.length; i++) {
			Object activity = myacis[i].getActivity();
			if (activity instanceof ServerTransaction) {
				ServerTransaction stx = (ServerTransaction) activity;
				Request req = stx.getRequest();
				if (!req.getMethod().equals(Request.CANCEL)
						&& req.getMethod().equals(clientTransaction.getRequest().getMethod()))
					return stx;
			}
		}
		return null;
	}
	
	
	
	private URI allocateRequestURI(String sipAddress){
		URI targetAddress = null;
		try {
			if(getFromURI().contains(sipAddress)){
	        	tracer.info("HERE is the conversion = "  + cleanUpRequestURI(getFromURI()));
				targetAddress = (addressFactory.createURI(cleanUpRequestURI(getFromURI())));
			}else if(getToURI().contains(sipAddress)){
				targetAddress = (addressFactory.createURI(cleanUpRequestURI(getToURI())));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return targetAddress;
	}
	
	private String cleanUpRequestURI(String address){
		String addressOfRecord = address;
		if(address.contains("<")){
			String []parts = addressOfRecord.split("<");
			String sudoAddress = parts[1];
			int index = sudoAddress.indexOf('>');
			StringBuilder sb = new StringBuilder(sudoAddress);
			sb.deleteCharAt(index);
			addressOfRecord = sb.toString();
		}
		return addressOfRecord;
	}


	// 'fromURI' CMP field setter
	public abstract void setFromURI(java.lang.String value);

	// 'fromURI' CMP field getter
	public abstract java.lang.String getFromURI();

	// 'toURI' CMP field setter
	public abstract void setToURI(java.lang.String value);

	// 'toURI' CMP field getter
	public abstract java.lang.String getToURI();

	// 'initialEvent' CMP field setter
	public abstract void setInitialEvent(javax.sip.RequestEvent value);

	// 'initialEvent' CMP field getter
	public abstract javax.sip.RequestEvent getInitialEvent();

	// 'dialogActivity' CMP field setter
	public abstract void setDialogActivity(javax.slee.ActivityContextInterface value);

	// 'dialogActivity' CMP field getter
	public abstract javax.slee.ActivityContextInterface getDialogActivity();

	// 'fromViaHeader' CMP field setter
	public abstract void setFromViaHeader(ViaHeader value);

	// 'fromViaHeader' CMP field getter
	public abstract ViaHeader getFromViaHeader();

	// 'incomingDialog' CMP field setter
	public abstract void setIncomingDialog(javax.slee.ActivityContextInterface value);

	// 'incomingDialog' CMP field getter
	public abstract javax.slee.ActivityContextInterface getIncomingDialog();

	// 'outgoingDialog' CMP field setter
	public abstract void setOutgoingDialog(javax.slee.ActivityContextInterface value);

	// 'outgoingDialog' CMP field getter
	public abstract javax.slee.ActivityContextInterface getOutgoingDialog();
	
	private ActivityContextInterface getPeerDialog(ActivityContextInterface aci) throws SipException {
        if (aci.equals(getIncomingDialog())) return getOutgoingDialog();
        if (aci.equals(getOutgoingDialog())) return getIncomingDialog();
        throw new SipException("could not find peer dialog");
    }

	public void onNOTIFY(javax.sip.RequestEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("Notify request received");
		processMidDialogRequest(event, aci);
	}

	public void onDialogSUBSCRIBE(javax.sip.RequestEvent event, com.arielProxyIMS.sbb.ProxyIMSSbbActivityContextInterface aci/*, EventContext eventContext*/) {
		tracer.info("Dialog subscription request received");
	}
}
