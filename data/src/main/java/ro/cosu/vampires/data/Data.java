package ro.cosu.vampires.data;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Data {


    private static final Logger LOG = LoggerFactory.getLogger(Data.class);

    public static void main(String[] args) throws IOException {

        List<File> results = Files.list(Paths.get(System.getProperty("user.dir")))
                .filter(p -> p.getFileName()
                        .toString().startsWith(""))
                .filter(p -> !p.toFile().isDirectory())
                .limit(1).map(Path::toFile)
                .peek(f -> LOG.info("{}", f))
                .collect(Collectors.toList());


        String logFile = results.get(0).getAbsoluteFile().toString(); // Should be some file on your system
        SparkConf conf = new SparkConf().setAppName("Simple Application").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(logFile).cache();



        long numAs = logData.filter((Function<String, Boolean>) s -> s.contains("a")).count();

        long numBs = logData.filter((Function<String, Boolean>) s -> s.contains("b")).count();


        LOG.info("Lines with a: " + numAs + ", lines with b: " + numBs);
    }
}
