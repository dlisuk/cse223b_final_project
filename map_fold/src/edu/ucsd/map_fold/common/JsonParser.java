package edu.ucsd.map_fold.common;

import ava.io.FileNotFoundException;
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

public class JsonParser {

    public JsonParser(String conf_path) throws IOException, ParseException{
       this.parser = new JSONParser();
       this.nodeData = (JSONObject) parser.parse(new FileReader(conf_path));
    }

	public int parseControllerPort(){

        JSONArray controllerArray = (JSONArray)nodeData.get("controller");
    	
        JSONObject node = (JSONObject)controllerArray.get(0);
    	int port = (Integer)node.get("port");
    	
		return port;
	}

    public int parseWorkerNum(){
        int workerNum = (Integer)nodeData.get("workers");
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
            int threads = (Integer)node.get("threads");
            workerAddrList.add(addr);
        }
        return workerAddrList;

    }

    public List parseTokens(){
        JSONArray tokenArray = (JSONArray)nodeData.get("tokens");
        List tokenList = new ArrayList<>();
        for(Object o : tokenArray){
            JSONObject token = (JSONObject) o;
            double mu = (Double)token.get("mu");
            double lambda = (Double)token.get("lambda");
            Integer[] fields = (Integer[])token.get("fields");
            List<Integer> fieldList = Arrays.asList(fields);
            Random tokenIdGen = new Random();
            Token tokenObject = new Token(tokenIdGen.nextInt(), fieldList, mu, lambda);
            tokenList.add(tokenObject);
        }
        return tokenList;
    }

    private JSONParser parser;
    private JSONObject nodeData;
}
