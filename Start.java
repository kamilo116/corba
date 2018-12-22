import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

class OptimizationImpl extends optimizationPOA implements optimizationOperations {

	class SingleServer implements Comparator<SingleServer>{
		public Short ip;
		public int timeout;
		public IntHolder id;
		private long timeFromLastHello;

		public SingleServer(Short ip, int timeout, IntHolder id) {
			this.ip = ip;
			this.timeout = timeout;
			this.timeFromLastHello = System.currentTimeMillis();
			this.id = id;
		}


		public boolean isActive() {
			return System.currentTimeMillis() - timeFromLastHello < timeout;
		}
		public void activate() {
			this.timeFromLastHello = System.currentTimeMillis();
		}

		@Override
		public int compare(SingleServer o1, SingleServer o2) {
			return o1.ip - o2.ip;
		}
	}

	private ConcurrentHashMap<IntHolder, SingleServer> servers = new ConcurrentHashMap<>();
	private List<ArrayList<Short>> addressRange = Collections.synchronizedList(new ArrayList<>()); //not used


	@Override
	public void register(short ip, int timeout, IntHolder id) {
		if(servers!=null && id != null){
			if (!servers.containsKey(id)){
				id.value = ip;
				servers.put(id, new SingleServer(ip, timeout, id));
			}else{
				if(servers.get(id) != null){
					servers.get(id).activate();
				}

			}
		}
	}


	@Override
	public void hello(int id) {
		if (servers.contains(id))
			servers.get(id).activate();
	}

	@Override
	public void best_range(rangeHolder r) {
		range bestRange = null, tmpRange = null;
		Iterator<SingleServer> it = servers.iterator();
		while (it.hasNext()) {
			SingleServer sItem = it.next();
			if (tmpRange == null && sItem.isActive()) {
				tmpRange = new range(sItem.ip, sItem.ip);
			} else if (tmpRange != null && sItem.isActive()) {
				if (sItem.ip - 1 == tmpRange.to) {
					tmpRange.to += 1;
				} else {
					tmpRange = new range(sItem.ip, sItem.ip);
				}
			} else {
				tmpRange = null;
			}
			if (bestRange == null || tmpRange != null && tmpRange.to - tmpRange.from > bestRange.to - bestRange.from) {
				bestRange = tmpRange;
			}
		}
		r.value = bestRange;
	}
}

class Start {

	public static void main(String[] args) {
		try {
			org.omg.CORBA.ORB orb = ORB.init(args, null);
			POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");
			rootpoa.the_POAManager().activate();

			OptimizationImpl optimizationImpl = new OptimizationImpl();
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(optimizationImpl);

			System.out.println(orb.object_to_string(ref));

			org.omg.CORBA.Object namingContextObj = orb.resolve_initial_references("NameService");
			NamingContext nCont = NamingContextHelper.narrow(namingContextObj);
			NameComponent[] path = { new NameComponent("Optymalizacja", "Object") };

			nCont.rebind(path, ref);
			orb.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
