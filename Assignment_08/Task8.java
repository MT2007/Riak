package Task8;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import java.net.UnknownHostException;



public class Task8 {

//  A basic Obj class to demonstrate typed exchanges with Riak
    public static class Song {
        public String singer;
        public String album;
        public String name;
        public Integer price;
    }

//  This will allow us to update the Song object handling the
//  entire fetch/modify/update cycle.
    public static class SongUpdate extends UpdateValue.Update<Song> {
        private final Song update;
        public SongUpdate(Song update){
            this.update = update;
        }

        @Override
        public Song apply(Song t) {
            if(t == null) {
                t = new Song();
            }

            t.singer = update.singer;
            t.album = update.album;
            t.name = update.name;
            t.price = update.price;

            return t;
        }
    }

//  This will create a client object that we can use to interact with Riak
    private static RiakCluster setUpCluster() throws UnknownHostException {
        RiakNode node = new RiakNode.Builder().withRemoteAddress("127.0.0.1").withRemotePort(8087).build();

//      This cluster object takes our one node as an argument
        RiakCluster cluster = new RiakCluster.Builder(node).build();

//      The cluster must be started to work, otherwise you will see errors
        cluster.start();

        return cluster;
    }

    public static void main(String[] args) {

    try {
        RiakCluster cluster = setUpCluster();
        RiakClient client = new RiakClient(cluster);
        System.out.println("Client object successfully created");

//      First, we'll create an object of type Song
        Song songObj = new Song();
        songObj.singer = "Adele";
        songObj.album = "Hello 2015";
        songObj.name = "Hello";
        songObj.price = 199;
        System.out.println("Song object created");

//      buckets you interact with Namespace
        Namespace songBucket = new Namespace("s21997_A08");

//      Location object,
        Location songObjectLocation = new Location(songBucket, "One");
        System.out.println("Location Song object created");

//      With our RiakObject in hand, we can create a StoreValue
        StoreValue storeSongOp = new StoreValue.Builder(songObj).withLocation(songObjectLocation).build();
        client.execute(storeSongOp);
        StoreValue.Response response = client.execute(storeSongOp);
        System.out.println("StoreValue operation created" + response);

//      Now we can verify that the object has been stored properly by
//      creating and executing a FetchValue operation
        FetchValue fetchOp = new FetchValue.Builder(songObjectLocation).build();
        Song fetchedSong = client.execute(fetchOp).getValue(Song.class);
        FetchValue.Response fresponse = client.execute(fetchOp);
        assert(songObj.getClass() == fetchedSong.getClass());
        assert(songObj.name.equals(fetchedSong.name));
        System.out.println("Song object successfully fetched: " + fresponse) ;

//      Now to update the song with additional copies
        songObj.price = 577;
        SongUpdate updatedsong= new SongUpdate(songObj);
        UpdateValue updateValue = new UpdateValue.Builder(songObjectLocation)
                .withUpdate(updatedsong).build();
        UpdateValue.Response Uresponse = client.execute(updateValue);
        System.out.println("Success! update the song check out: " + Uresponse);

//      Now we can verify that the object has been stored properly by
//      creating and executing a FetchValue operation
        FetchValue ufetchOp = new FetchValue.Builder(songObjectLocation).build();
        Song ufetchedSong = client.execute(ufetchOp).getValue(Song.class);
        FetchValue.Response ufresponse = client.execute(ufetchOp);
        System.out.println("Song object successfully fetched: " + ufresponse) ;


//      And we'll delete a Song object
        DeleteValue deleteOp = new DeleteValue.Builder(songObjectLocation).build();
        client.execute(deleteOp);
        System.out.println("Song object successfully deleted");

//      Now we can verify that the object has been stored properly
        FetchValue dufetchOp = new FetchValue.Builder(songObjectLocation).build();
        Song dufetchedSong = client.execute(dufetchOp).getValue(Song.class);
        FetchValue.Response dufresponse = client.execute(dufetchOp);
        System.out.println("Song object successfully fetched: " + dufresponse) ;


//      Now that we're all finished, we should shut our cluster object down
        cluster.shutdown();
        }catch (Exception e) {
        System.out.println(e.getMessage());
      }

    }

}

