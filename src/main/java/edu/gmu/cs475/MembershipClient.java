package edu.gmu.cs475;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.nodes.PersistentNode;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.util.HashMap;

public class MembershipClient {


	int debug;
	private CuratorFramework zk;

	public MembershipClient(String zkConnectStr, int debug) {
		this.debug = debug;
		zk = CuratorFrameworkFactory.newClient(zkConnectStr, new RetryOneTime(100));
		zk.getConnectionStateListenable().addListener((client, newState) -> {
			System.out.println("Client " + debug + " connection state changed: " + newState);
		});
		zk.start();

	}

	HashMap<String, PersistentNode> groupMemberships = new HashMap<>();
	HashMap<String, TreeCache> groupLists = new HashMap<>();
	public void joinGroup(String groupName, String id) {

		PersistentNode myMembership = new PersistentNode(zk, CreateMode.EPHEMERAL, false, "/memberships/" + groupName + "/" + id, new byte[0]);
		myMembership.start();
		groupMemberships.put(groupName,myMembership);

		TreeCache members = new TreeCache(zk, "/memberships/" + groupName);
		members.getListenable().addListener((client, event) -> {
			System.out.println("Client " + debug + " Membership change detected: " + event
			);
		});
		try {
			members.start();

			groupLists.put(groupName,members);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void leaveGroup(String groupName) {
		try {
			groupMemberships.remove(groupName).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String listMembers(String groupName) {
		return groupLists.get(groupName).getCurrentChildren("/memberships/"+groupName).toString();
	}

	public void cleanup() {

	}
}



