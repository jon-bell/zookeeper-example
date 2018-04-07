package edu.gmu.cs475.internal;

import edu.gmu.cs475.MembershipClient;
import org.netcrusher.NetCrusher;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.function.Function;

public class TestingClient extends MembershipClient {

	boolean zkDown;
	private NetCrusher proxyToZK;

	private int debug;

	public TestingClient(String zkConnectString, NetCrusher proxyToZK, int debug) {
		super(zkConnectString, debug);
		this.debug = debug;
		this.proxyToZK = proxyToZK;
	}

	public void suspendAccessToZK() {
		this.proxyToZK.freeze();
		zkDown = true;
	}

	public void resumeAccessToZK() {
		zkDown = false;
		this.proxyToZK.unfreeze();
	}

	public void cleanup() {
		proxyToZK.close();
	}

	@Override
	public String toString() {
		return "Client #"+debug + ", connected to ZooKeeper " + (zkDown?"N":"Y");
	}
}
