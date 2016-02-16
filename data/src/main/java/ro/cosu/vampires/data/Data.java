package ro.cosu.vampires.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import ro.cosu.vampires.server.util.gson.AutoValueAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableListTypeAdapterFactory;
import ro.cosu.vampires.server.util.gson.ImmutableMapTypeAdapterFactory;
import ro.cosu.vampires.server.writers.json.AllResults;
import ro.cosu.vampires.server.writers.json.LocalDateTimeDeserializer;
import ro.cosu.vampires.server.writers.json.LocalDateTimeSerializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.LogManager;

public class Data {


    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    private static String jsonFile = "/home/cdumitru/results.json";
    private static String jsonFile1 = "/home/cdumitru/results-all-2016-02-16T21:52:07.349.json";

    private static AllResults loadFromJson() throws FileNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDateTime.class ,new LocalDateTimeDeserializer())
                .registerTypeAdapterFactory(new AutoValueAdapterFactory())
                .registerTypeAdapterFactory(new ImmutableMapTypeAdapterFactory())
                .registerTypeAdapter(ImmutableMap.class, ImmutableMapTypeAdapterFactory.newCreator())
                .registerTypeAdapterFactory(new ImmutableListTypeAdapterFactory())
                .create();

        AllResults response = gson.fromJson(new FileReader(jsonFile1), AllResults.class);

        response.results().stream().forEach(job -> {
            job.hostMetrics().metrics().stream().forEach(metric -> {
                LOG.info("time: {}", metric.time());
                LOG.info("values: {}", metric.values());

            });

        });

        return response;

    }


    private static final Logger LOG = LoggerFactory.getLogger(Data.class);

    public static void main(String[] args) throws IOException {

        loadFromJson();
        SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(jsonFile).cache();


        long numAs = logData.filter(s -> s.contains("a")).count();

        long numBs = logData.filter(s -> s.contains("b")).count();

        LOG.info("Lines with a: " + numAs + ", lines with b: " + numBs);


        SQLContext sqlContext = new org.apache.spark.sql.SQLContext(sc);
        DataFrame df = sqlContext.read().json(jsonFile);

        AllResults allResults = loadFromJson();
//        sqlContext.createDataFrame()
        df.show();

        df.printSchema();

        df.filter("result.duration < 4517").show();
//


    }
}
