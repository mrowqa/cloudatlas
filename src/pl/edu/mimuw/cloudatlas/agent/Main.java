/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Duration;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterException;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueBoolean;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueList;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;


/**
 *
 * @author pawel
 */
public class Main {
	private static Duration queryDuration = Duration.seconds(5);
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		parseDuration(args);
		if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
		try {
			CloudAtlasAgent agent = new CloudAtlasAgent(createTestHierarchy(), queryDuration);
			CloudAtlasInterface stub = (CloudAtlasInterface) UnicastRemoteObject.exportObject(agent, 0);
			Registry registry = LocateRegistry.getRegistry();
            registry.rebind("CloudAtlas", stub);
			System.out.println("CloudAtlas Agent bound");
		} catch (Exception ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private static void parseDuration(String[] args) {
		System.out.println("Args lenght " + args.length);
		if (args.length < 1) {
			return;
		}
		char durationUnit = args[0].charAt(args[0].length() - 1);
		int durationValue = Integer.parseInt(args[0].substring(0, args[0].length() - 1));
		switch(durationUnit) {
			case 'h': 
				queryDuration = Duration.hours(durationValue); 
				break;
			case 'm':
				queryDuration = Duration.minutes(durationValue); 
				break;
			case 's':
				System.out.println("Seconds parsed");
				queryDuration = Duration.seconds(durationValue); 
				break;
			default:
				System.err.println("Invalid duration unit, got " + durationUnit + " allowed h|m|s.");
				System.exit(1);
		}
	}
	
	private static ValueContact createContact(String path, byte ip1, byte ip2, byte ip3, byte ip4)
			throws UnknownHostException {
		return new ValueContact(new PathName(path), InetAddress.getByAddress(new byte[] {
			ip1, ip2, ip3, ip4
		}));
	}
	
	private static ZMI createTestHierarchy() throws ParseException, UnknownHostException {
		ZMI root;
		ValueContact violet07Contact = createContact("/uw/violet07", (byte)10, (byte)1, (byte)1, (byte)10);
		ValueContact khaki13Contact = createContact("/uw/khaki13", (byte)10, (byte)1, (byte)1, (byte)38);
		ValueContact khaki31Contact = createContact("/uw/khaki31", (byte)10, (byte)1, (byte)1, (byte)39);
		ValueContact whatever01Contact = createContact("/uw/whatever01", (byte)82, (byte)111, (byte)52, (byte)56);
		ValueContact whatever02Contact = createContact("/uw/whatever02", (byte)82, (byte)111, (byte)52, (byte)57);
		
		List<Value> list;
		
		root = new ZMI();
		root.getAttributes().add("level", new ValueInt(0l));
		root.getAttributes().add("owner", new ValueString("/uw/violet07"));
		root.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:10:17.342"));
		root.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		root.getAttributes().add("cardinality", new ValueInt(0l));
		
		ZMI uw = new ZMI(root, "uw");
		root.addSon(uw);
		uw.getAttributes().add("level", new ValueInt(1l));
		uw.getAttributes().add("owner", new ValueString("/uw/violet07"));
		uw.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
		uw.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		uw.getAttributes().add("cardinality", new ValueInt(0l));
		
		ZMI pjwstk = new ZMI(root, "pjwstk");
		root.addSon(pjwstk);
		pjwstk.getAttributes().add("level", new ValueInt(1l));
		pjwstk.getAttributes().add("owner", new ValueString("/pjwstk/whatever01"));
		pjwstk.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:8:13.123"));
		pjwstk.getAttributes().add("contacts", new ValueSet(TypePrimitive.CONTACT));
		pjwstk.getAttributes().add("cardinality", new ValueInt(0l));
		
		ZMI violet07 = new ZMI(uw, "violet07");
		uw.addSon(violet07);
		violet07.getAttributes().add("level", new ValueInt(2l));
		violet07.getAttributes().add("owner", new ValueString("/uw/violet07"));
		violet07.getAttributes().add("timestamp", new ValueTime("2012/11/09 18:00:00.000"));
		list = Arrays.asList(new Value[] {
			khaki31Contact, whatever01Contact
		});
		violet07.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			violet07Contact,
		});
		violet07.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		violet07.getAttributes().add("creation", new ValueTime("2011/11/09 20:8:13.123"));
		violet07.getAttributes().add("cpu_usage", new ValueDouble(0.9));
		violet07.getAttributes().add("num_cores", new ValueInt(3l));
		violet07.getAttributes().add("has_ups", new ValueBoolean(null));
		list = Arrays.asList(new Value[] {
			new ValueString("tola"), new ValueString("tosia"),
		});
		violet07.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		violet07.getAttributes().add("expiry", new ValueDuration(13l, 12l, 0l, 0l, 0l));
		
		ZMI khaki31 = new ZMI(uw, "khaki31");
		uw.addSon(khaki31);
		khaki31.getAttributes().add("level", new ValueInt(2l));
		khaki31.getAttributes().add("owner", new ValueString("/uw/khaki31"));
		khaki31.getAttributes().add("timestamp", new ValueTime("2012/11/09 20:03:00.000"));
		list = Arrays.asList(new Value[] {
			violet07Contact, whatever02Contact,
		});
		khaki31.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			khaki31Contact
		});
		khaki31.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki31.getAttributes().add("creation", new ValueTime("2011/11/09 20:12:13.123"));
		khaki31.getAttributes().add("cpu_usage", new ValueDouble(null));
		khaki31.getAttributes().add("num_cores", new ValueInt(3l));
		khaki31.getAttributes().add("has_ups", new ValueBoolean(false));
		list = Arrays.asList(new Value[] {
			new ValueString("agatka"), new ValueString("beatka"), new ValueString("celina"),
		});
		khaki31.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki31.getAttributes().add("expiry", new ValueDuration(-13l, -11l, 0l, 0l, 0l));
		
		ZMI khaki13 = new ZMI(uw, "khaki13");
		uw.addSon(khaki13);
		khaki13.getAttributes().add("level", new ValueInt(2l));
		khaki13.getAttributes().add("owner", new ValueString("/uw/khaki13"));
		khaki13.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:03:00.000"));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			khaki13Contact,
		});
		khaki13.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		khaki13.getAttributes().add("creation", new ValueTime((Long)null));
		khaki13.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		khaki13.getAttributes().add("num_cores", new ValueInt(null));
		khaki13.getAttributes().add("has_ups", new ValueBoolean(true));
		list = Arrays.asList(new Value[] {});
		khaki13.getAttributes().add("some_names", new ValueList(list, TypePrimitive.STRING));
		khaki13.getAttributes().add("expiry", new ValueDuration((Long)null));
		
		ZMI whatever01 = new ZMI(pjwstk, "whatever01");
		pjwstk.addSon(whatever01);
		whatever01.getAttributes().add("level", new ValueInt(2l));
		whatever01.getAttributes().add("owner", new ValueString("/uw/whatever01"));
		whatever01.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:12:00.000"));
		list = Arrays.asList(new Value[] {
			violet07Contact, whatever02Contact,
		});
		whatever01.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			whatever01Contact,
		});
		whatever01.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever01.getAttributes().add("creation", new ValueTime("2012/10/18 07:03:00.000"));
		whatever01.getAttributes().add("cpu_usage", new ValueDouble(0.1));
		whatever01.getAttributes().add("num_cores", new ValueInt(7l));
		list = Arrays.asList(new Value[] {
			new ValueString("rewrite")
		});
		whatever01.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));
		
		ZMI whatever02 = new ZMI(pjwstk, "whatever02");
		pjwstk.addSon(whatever02);
		whatever02.getAttributes().add("level", new ValueInt(2l));
		whatever02.getAttributes().add("owner", new ValueString("/uw/whatever02"));
		whatever02.getAttributes().add("timestamp", new ValueTime("2012/11/09 21:13:00.000"));
		list = Arrays.asList(new Value[] {
			khaki31Contact, whatever01Contact,
		});
		whatever02.getAttributes().add("contacts", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("cardinality", new ValueInt(1l));
		list = Arrays.asList(new Value[] {
			whatever02Contact,
		});
		whatever02.getAttributes().add("members", new ValueSet(new HashSet<Value>(list), TypePrimitive.CONTACT));
		whatever02.getAttributes().add("creation", new ValueTime("2012/10/18 07:04:00.000"));
		whatever02.getAttributes().add("cpu_usage", new ValueDouble(0.4));
		whatever02.getAttributes().add("num_cores", new ValueInt(13l));
		list = Arrays.asList(new Value[] {
			new ValueString("odbc")
		});
		whatever02.getAttributes().add("php_modules", new ValueList(list, TypePrimitive.STRING));
		
		return root;
	}
	
}
