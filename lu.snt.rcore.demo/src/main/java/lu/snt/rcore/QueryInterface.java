package lu.snt.rcore;

/**
 * Created with IntelliJ IDEA.
 * User: assaad.moawad
 * Date: 15/05/13
 * Time: 14:33
 * To change this template use File | Settings | File Templates.
 */

import lu.snt.rcore.agencies.Query;
import lu.snt.rcore.agencies.QueryResponse;

public interface QueryInterface {
    public void sendResults(QueryResponse q, int currentProcessId);

    public String getName();

    public void notifyFailure(Query q, int currentProcessId);

    public void sendQueryToPeer(Query q);

    public void consoleOutput(Object o);

}
