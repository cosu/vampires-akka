package server;

import akka.actor.UntypedActor;
import akka.util.ByteString;

/**
 * User: Cosmin 'cosu' Dumitru - cosu@cosu.ro
 * Date: 9/13/15
 * Time: 9:39 PM
 */
public class ResultManager extends UntypedActor{
    @Override
    public void onReceive(Object msg) throws Exception {
        if ( msg instanceof ByteString) {
            //this is data

        }
    }
}
