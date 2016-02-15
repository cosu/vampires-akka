package ro.cosu.vampires.data;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.util.logging.LogManager;

public class Data {


    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    private static String jsonFile = "/home/cdumitru/results.json";

    private static final Logger LOG = LoggerFactory.getLogger(Data.class);

    public static void main(String[] args) throws IOException {


        SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(jsonFile).cache();

        long numAs = logData.filter(s -> s.contains("a")).count();

        long numBs = logData.filter(s -> s.contains("b")).count();

        LOG.info("Lines with a: " + numAs + ", lines with b: " + numBs);


        SQLContext sqlContext = new org.apache.spark.sql.SQLContext(sc);
        DataFrame df = sqlContext.read().json(jsonFile);

        df.show();

        df.printSchema();


    }
}
