package com.aws;

import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class get_metrics_data {
    static List<Double> a1 = new ArrayList<>();
    static List<Instant> a2 = new ArrayList<>();
    static String tit;
    public static class LineChartExample extends JFrame {

        private static final long serialVersionUID = 1L;

        public LineChartExample(String title) {
            super(title);
            // Create dataset
            DefaultCategoryDataset dataset = createDataset();
            // Create chart
            JFreeChart chart = ChartFactory.createLineChart(
                    tit, // Chart title
                    "Date and Time", // X-Axis Label
                    "Average Value", // Y-Axis Label
                    dataset
            );
            ChartPanel chartPanel = new ChartPanel(chart) ;

            chartPanel.addChartMouseListener(new ChartMouseListener() {

                @Override
                public void chartMouseClicked(ChartMouseEvent e) {
                    final ChartEntity entity = e.getEntity();
                    System.out.println(entity + " " + entity.getArea());
                }

                @Override
                public void chartMouseMoved(ChartMouseEvent e) {
                }
            });
            chart.setBackgroundPaint(Color.white);
            ChartUtilities.applyCurrentTheme(chart);
            final CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.black);
            plot.setRangeGridlinePaint(Color.white);
            CategoryAxis catAxis = plot.getDomainAxis();
            CategoryItemRenderer rendu = plot.getRenderer();
            rendu.setSeriesPaint(0, new Color(255,255,100));
            catAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
            catAxis.setTickLabelPaint(new Color(0,0,0,0));
            int y1=0;
            int frame =5;
            int hour = 900;
            y1=5*hour/24;

            for(int i=0;i<a2.size();i=i+y1)
            {
                //String cat_Name = (String) plot.getCategories().get(i-1);
                catAxis.setTickLabelPaint(a2.get(i), Color.black);
            }
            Font nwfont=new Font("Arial",0,10);
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setTickLabelFont(nwfont);
            catAxis.setTickLabelFont(nwfont);
            File imageFile = new File("LineChart.png");
            int width = 1200;
            int height = 700;
            plot.setOutlinePaint(Color.GRAY);
            plot.setOutlineStroke(new BasicStroke(2.0f));
            try {
                ChartUtilities.saveChartAsPNG(imageFile, chart, width, height);
            } catch (IOException ex) {
                System.err.println(ex);
            }
            ChartPanel panel = new ChartPanel(chart);
            setContentPane(panel);
        }
    }

    private static DefaultCategoryDataset createDataset() {
        String series1 = tit;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int i=0;i<a1.size();i++)
        {
            dataset.addValue(a1.get(i),series1, a2.get(i));
        }
        return dataset;
    }

    public static void main(String[] args) {
        Region region = Region.US_WEST_1;
        CloudWatchClient cw = CloudWatchClient.builder()
                .region(region)

                .build();

        getMetData(cw) ;
        cw.close();
    }


    public static void getMetData(CloudWatchClient cw) {

        try {
            // Set the date
            System.out.println("Enter the BucketName:");
            String bucket;
            Scanner sc1 = new Scanner(System.in);
            bucket = sc1.nextLine();

            Instant start = Instant.parse("2022-01-02T10:12:35Z");
            Instant endDate = Instant.now();

            Dimension n1 = Dimension.builder()
                    .name("BucketName")
                    .value(bucket)
                    .build();
            //juhig-uswest1;



            //StandardStorage

            String stor;
            System.out.println("Enter the name of the metric :");
            Scanner sc3 =new Scanner(System.in);
            String metric_name = sc3.nextLine();
            tit = metric_name;

            //BucketSizeBytes
            //AWS/S3
            if(metric_name.equals("BucketSizeBytes"))
            {
                stor="StandardStorage";
            }
            else
            {
                stor="AllStorageTypes";
            }
            stor="EntireBucket";

            Dimension n2 = Dimension.builder()
                    .name("FilterId")
                    .value(stor)
                    .build();

            /*System.out.println("Enter the Namespace:");
            Scanner sc4 = new Scanner(System.in);*/
            String name = "AWS/S3";
            Metric met = Metric.builder()
                    .metricName(metric_name)
                    .namespace(name)
                    .dimensions(n1,n2)
                    .build();
           /* System.out.println("Enter the aggregation type:");
            Scanner sc5 = new Scanner(System.in);
            String arg = sc5.nextLine();*/
            MetricStat metStat = MetricStat.builder()
                    .stat("Average")
                    .period(300)
                    .metric(met)
                    .build();

            MetricDataQuery dataQUery = MetricDataQuery.builder()
                    .metricStat(metStat)
                    .returnData(true)
                    .id("foo2")
                    .build();

            List<MetricDataQuery> dq = new ArrayList();
            dq.add(dataQUery);

            GetMetricDataRequest getMetReq = GetMetricDataRequest.builder()
                    .maxDatapoints(100800)
                    .startTime(start)
                    .endTime(endDate)
                    .metricDataQueries(dq)
                    .scanBy("TimestampAscending")
                    .build();

            GetMetricDataResponse response = cw.getMetricData(getMetReq);
            List<MetricDataResult> data = response.metricDataResults();

            try {
                PrintWriter out = new PrintWriter(new FileWriter("y.txt"));
                PrintWriter out1 = new PrintWriter(new FileWriter("x.txt"));
                for (int i = 0; i < data.size(); i++) {
                    MetricDataResult item = (MetricDataResult) data.get(i);
                    // System.out.println(item.id());

                    List<Double> it = item.values();
                    List<Instant> it1 = item.timestamps();
                    for(int j=0;j<it.size();j++)
                    {
                        out.println(it.get(j));
                        out1.println(it1.get(j));
                        a1.add(it.get(j));
                        a2.add(it1.get(j));
                        System.out.print(it.get(j));
                        System.out.print(" ");
                        System.out.println(it1.get(j));
                    }

                }

                out.close();
                out1.close();

                //Process p = Runtime.getRuntime().exec("new.py");
            }
            catch(IOException e1) {
                System.out.println("Error during reading/writing");
            }

            //Process p = Runtime.getRuntime().exec("/usr/bin/python3 /Users/vatsal.gujarati/Desktop/AWS_METRICS/new.py");
            SwingUtilities.invokeLater(() -> {
                LineChartExample example = new LineChartExample("Metrics");
                example.setAlwaysOnTop(true);
                example.pack();
                example.setSize(1200, 700);
                example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                example.setVisible(true);
                int domainAxis = example.getX();

            });
        } catch (CloudWatchException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

}
