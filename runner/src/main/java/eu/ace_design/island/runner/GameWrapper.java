package eu.ace_design.island.runner;

import eu.ace_design.island.game.Game;
import eu.ace_design.island.game.GameBoard;
import eu.ace_design.island.map.IslandMap;
import eu.ace_design.island.map.resources.Resource;
import org.json.JSONArray;
import org.json.JSONObject;
import scala.Enumeration;
import scala.Tuple2;
import scala.collection.JavaConversions$;

import java.util.HashMap;
import java.util.Map;

/**
 * PolyTech Nice / SI3 / Module POO-Java
 * Annee 2015 - island - Lab 3
 * Package eu.ace_design.island.runner
 *
 * @author Flavian Jacquot
 * @version 17/07/2016
 * @since 1.8.0_60
 */
public class GameWrapper {
    private Game game;
    private GameBoard gb;
    private IslandMap theIsland;
    private Map<String,Integer> collected;

    public long getMs() {
        return ms;
    }

    public void setMs(long ms) {
        this.ms = ms;
    }

    private long ms = 0;

    public GameWrapper(Game game, GameBoard bord, IslandMap theIsland) {
        this.game = game;
        this.gb = bord;
        this.theIsland = theIsland;

        collected = new HashMap<>();
        for(Resource r: JavaConversions$.MODULE$.asJavaCollection(game.collectedResources().keys())) {
            Integer amount = (Integer)game.collectedResources().get(r).get();
            System.out.println("Add res"+amount+" "+r);
            System.out.println("Add res"+game.collectedResources().get(r).get().getClass()+" "+r.getClass());
            collected.put(r.name(),amount);
        }
    }

    public Map<String,Integer> collected()
    {
        return collected;
    }
    public JSONArray getCollected()
    {
        JSONArray col = new JSONArray();

        collected.forEach((k,v)->{
            JSONObject line = new JSONObject();
            line.put("res",k);
            line.put("amount",v);
            col.put(line);
        });

        return col;
    }

    public int getVisited()
    {
        return game.visited().size();
    }

    public int getScanned()
    {
        return game.scanned().size();
    }

    public int getContractMax()
    {
        return game.objectives().size();
    }

    public String getResult()
    {
        return game.isOK()?"OK":"KO";
    }

    public int getInitial()
    {
        return game.budget().initial();
    }

    public int getRemaining()
    {
        return game.budget().remaining();
    }

    public Map<String,String> getStats()
    {
        Map<String,String> map = new HashMap<>();
        for(Tuple2<Enumeration.Value,String> evt: JavaConversions$.MODULE$.asJavaIterable(theIsland.stats().get())) {
            map.put(evt._1.toString(),evt._2);
        }
        System.out.println("Stats");
        return map;
    }

    public int getSize()
    {
        return game.budget().remaining();
    }
    public JSONObject toJson()
    {
        return new JSONObject(this);
    }
    /*
        {
        contract-completed:2,
        contracts-max:4,
        result:"ok",
        initial:false,
        remaining:false,
        visited:232,
        scanned:90,
        size:1000}
         */
}
