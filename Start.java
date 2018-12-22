import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CORBA.IntHolder;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;

import com.sun.corba.se.org.omg.CORBA.ORB;

class OptimizationImpl extends optimizationPOA implements optimizationOperations {

    class SingleServer {
        private short ip;
        private int id;
        private int timeout;
        private long lastHello;

        public SingleServer(int id, short ip, int timeout) {
            this.id = id;
            this.ip = ip;
            this.timeout = timeout;
        }


        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public boolean isActive() {
            return System.currentTimeMillis() - lastHello < timeout;
        }

        public void activate() {
            lastHello = System.currentTimeMillis();
        }


    }

    class SingleServerIpComparator implements Comparator<SingleServer> {
        @Override
        public int compare(SingleServer o1, SingleServer o2) {
            return o1.ip - o2.ip;
        }
    }

    static AtomicInteger idCount = new AtomicInteger(0);

    private ConcurrentHashMap<Integer, SingleServer> serversID = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Short, SingleServer> serversIP = new ConcurrentHashMap<>();
    private ConcurrentSkipListSet<SingleServer> servers = new ConcurrentSkipListSet<SingleServer>(new SingleServerIpComparator());

    @Override
    public void register(short ip, int timeout, IntHolder id) {
        SingleServer serverItem = serversIP.get(ip);

        if (serverItem != null) {
            serverItem.setTimeout(timeout);// czy pottrzebne
            id.value = serverItem.id;
        } else {
            id.value = idCount.getAndIncrement();
            serverItem = new SingleServer(id.value, ip, timeout);
            serverItem.activate();
            serversIP.put(ip, serverItem);
            serversID.put(id.value, serverItem);
            servers.add(serverItem);
        }
    }

    @Override
    public void hello(int id) {
        SingleServer serverItem = serversID.get(id);
        if (serverItem != null) {
            serverItem.activate();
        }
    }

    @Override
    public void best_range(rangeHolder r) {
        range bestRange = null, tmpRange = null;
        while (SingleServer sItem : servers) {
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
