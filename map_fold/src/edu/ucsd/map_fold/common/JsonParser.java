package edu.ucsd.map_fold.common;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import edu.ucsd.map_fold.common.logistic_regression.Token;
import edu.ucsd.map_fold.common.WorkerConf;
public class JsonParser {

    public JsonParser(String conf_path) throws IOException, ParseException{
       this.parser = new JSONParser();
       System.out.println(conf_path);
       this.nodeData = (JSONObject) parser.parse(new FileReader(conf_path));
    }

	public String parseControllerPort(){

        JSONArray controllerArray = (JSONArray)nodeData.get("controller");
    	
        JSONObject node = (JSONObject)controllerArray.get(0);
    	String port = (String)node.get("port");
    	
		return port;
	}

    public long parseWorkerNum(){
        long workerNum = (Long)nodeData.get("workers");
        return workerNum;
    }

    public String parseDataPath(){
        String dataPath = (String)nodeData.get("data");
        return dataPath;
    }

    public List parseWorkerAddr(){
        JSONArray nodeArray = (JSONArray)nodeData.get("worker");
        List workerAddrList = new ArrayList<>();
        for(Object o : nodeArray){
            JSONObject node = (JSONObject) o;
            String addr = (String)node.get("addr");
            String port = (String)node.get("port");
            int threads = safeLongToInt((Long)node.get("threads"));
            WorkerConf wf = new WorkerConf(addr, port, threads);
            workerAddrList.add(wf);
        }
        return workerAddrList;

    }


    public List<Token> parseTokens(){
        int tokenId = 0;
        JSONArray tokenArray = (JSONArray)nodeData.get("tokens");
        List<Token> tokenList = new ArrayList<>();
        for(Object o : tokenArray){
            JSONObject token = (JSONObject) o;
            double mu = (Double)token.get("mu");
            double lambda = (Double)token.get("lambda");
            List<Integer> fieldList = (List)token.get("fields");
            Token tokenObject = new Token(tokenId, fieldList, mu, lambda);
            tokenList.add(tokenObject);
            tokenId++;
        }
        return tokenList;
    }

    public int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    private JSONParser parser;
    private JSONObject nodeData;
}
