package org.jlab;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jlab.mya.DataNexus;
import org.jlab.mya.Deployment;
import org.jlab.mya.Metadata;
import org.jlab.mya.event.IntEvent;
import org.jlab.mya.nexus.OnDemandNexus;
import org.jlab.mya.params.IntervalQueryParams;
import org.jlab.mya.service.IntervalService;
import org.jlab.mya.stream.IntEventStream;

/**
 *
 * @author ryans
 */
public class Trippy {

    public Trippy() throws SQLException, IOException {
        DataNexus nexus = new OnDemandNexus(Deployment.ops);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        IntervalService service = new IntervalService(nexus);

        final int MAX_RECOVERY_SECONDS = 3600; // 1 Hour
        final String masterFsdNodePv = "ISD0I011G";
        final String hallARecoveryPv = "HLA:bta_bm_present";
        final String hallBRecoveryPv = "HLB:bta_bm_present";
        final String hallCRecoveryPv = "HLC:bta_bm_present";
        final String hallDRecoveryPv = "HLD:bta_bm_present";

        Instant begin = LocalDateTime.parse("2017-01-01T00:00:00.123456").atZone(ZoneId.systemDefault()).toInstant();
        Instant end = LocalDateTime.parse("2019-01-01T00:01:00.123456").atZone(ZoneId.systemDefault()).toInstant();

        //TreeSet<Instant> masterRecoverySet = getBinaryPoint(service, masterFsdNodePv, begin, end, false);
        //TreeSet<Instant> masterTripSet = getBinaryPoint(service, masterFsdNodePv, begin, end, true);
        
        List<TreeSet<Instant>> masterPoints = getBinaryPoint(service, masterFsdNodePv, begin, end);
        TreeSet<Instant> masterRecoverySet = masterPoints.get(0);
        TreeSet<Instant> masterTripSet = masterPoints.get(1);

        TreeSet<Instant> hallARecoverySet = getBinaryPoint(service, hallARecoveryPv, begin, end, true);
        TreeSet<Instant> hallBRecoverySet = getBinaryPoint(service, hallBRecoveryPv, begin, end, true);
        TreeSet<Instant> hallCRecoverySet = getBinaryPoint(service, hallCRecoveryPv, begin, end, true);
        TreeSet<Instant> hallDRecoverySet = getBinaryPoint(service, hallDRecoveryPv, begin, end, true);

        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Trip Recovery");

        CreationHelper createHelper = wb.getCreationHelper();
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-MMM-dd HH:mm:ss"));

        int rowCounter = 0;
        Row row1 = sheet1.createRow(rowCounter++);
        row1.createCell(0).setCellValue("TRIP DOWN");
        row1.createCell(1).setCellValue("TRIP CLEAR");
        row1.createCell(2).setCellValue("HALL A RECOVERY");
        row1.createCell(3).setCellValue("HALL B RECOVERY");
        row1.createCell(4).setCellValue("HALL C RECOVERY");
        row1.createCell(5).setCellValue("TRIP SECONDS");
        row1.createCell(6).setCellValue("HALL A RECOVERY SECONDS");
        row1.createCell(7).setCellValue("HALL B RECOVERY SECONDS");
        row1.createCell(8).setCellValue("HALL C RECOVERY SECONDS");
        row1.createCell(9).setCellValue("HALL D RECOVERY SECONDS");        
        
        
        //((XSSFSheet) sheet1).getColumnHelper().setColDefaultStyle(0, dateStyle);
        //((XSSFSheet) sheet1).getColumnHelper().setColDefaultStyle(1, dateStyle);
        
        for (Instant tripClear : masterRecoverySet) {

            Instant tripDown = masterTripSet.lower(tripClear);
            Instant nextTrip = masterTripSet.higher(tripClear);

            //System.out.println(tripDown);
            
            Instant hallARecoveryEnd = hallARecoverySet.higher(tripClear);
            Instant hallBRecoveryEnd = hallBRecoverySet.higher(tripClear);
            Instant hallCRecoveryEnd = hallCRecoverySet.higher(tripClear);
            Instant hallDRecoveryEnd = hallDRecoverySet.higher(tripClear);

            if (nextTrip != null) {
                if (hallARecoveryEnd != null && nextTrip.getEpochSecond() < hallARecoveryEnd.getEpochSecond()) {
                    hallARecoveryEnd = nextTrip;
                }

                if (hallBRecoveryEnd != null && nextTrip.getEpochSecond() < hallBRecoveryEnd.getEpochSecond()) {
                    hallBRecoveryEnd = nextTrip;
                }

                if (hallCRecoveryEnd != null && nextTrip.getEpochSecond() < hallCRecoveryEnd.getEpochSecond()) {
                    hallCRecoveryEnd = nextTrip;
                }

                if (hallDRecoveryEnd != null && nextTrip.getEpochSecond() < hallDRecoveryEnd.getEpochSecond()) {
                    hallDRecoveryEnd = nextTrip;
                }
            }

            Duration hallARecovery = hallARecoveryEnd == null ? null : Duration.between(tripClear, hallARecoveryEnd);
            Duration hallBRecovery = hallBRecoveryEnd == null ? null : Duration.between(tripClear, hallBRecoveryEnd);
            Duration hallCRecovery = hallCRecoveryEnd == null ? null : Duration.between(tripClear, hallCRecoveryEnd);
            Duration hallDRecovery = hallDRecoveryEnd == null ? null : Duration.between(tripClear, hallDRecoveryEnd);

            //System.out.println("row: " + rowCounter);
            Row row = sheet1.createRow(rowCounter++);

            Cell c;
            
            if (tripDown != null) {
                c = row.createCell(0);
                c.setCellValue(Date.from(tripDown));
                c.setCellStyle(dateStyle);
            }

            c = row.createCell(1);
            c.setCellValue(Date.from(tripClear));
            c.setCellStyle(dateStyle);
            
            if (tripDown != null && tripClear != null) {
                Duration tripRecovery = Duration.between(tripDown, tripClear);
                row.createCell(5).setCellValue(tripRecovery.getSeconds());
            }

            //System.out.println(formatter.format(tripClear) + " - ");
            if (hallARecovery != null && hallARecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                row.createCell(2).setCellValue(formatter.format(hallARecoveryEnd));
                row.createCell(6).setCellValue(hallARecovery.getSeconds());
            }

            if (hallBRecovery != null && hallBRecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                row.createCell(3).setCellValue(formatter.format(hallBRecoveryEnd));
                row.createCell(7).setCellValue(hallBRecovery.getSeconds());
            }

            if (hallCRecovery != null && hallCRecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                row.createCell(3).setCellValue(formatter.format(hallCRecoveryEnd));
                row.createCell(7).setCellValue(hallCRecovery.getSeconds());
            }

            if (hallDRecovery != null && hallDRecovery.getSeconds() < MAX_RECOVERY_SECONDS) {
                row.createCell(3).setCellValue(formatter.format(hallDRecoveryEnd));
                row.createCell(7).setCellValue(hallDRecovery.getSeconds());
            }
        }

        /*sheet1.autoSizeColumn(0);
        sheet1.autoSizeColumn(1);
        sheet1.autoSizeColumn(2);
        sheet1.autoSizeColumn(3);
        sheet1.autoSizeColumn(4);
        sheet1.autoSizeColumn(5);
        sheet1.autoSizeColumn(6);
        sheet1.autoSizeColumn(7);
        sheet1.autoSizeColumn(8);
        sheet1.autoSizeColumn(9);*/
        
        try (FileOutputStream out = new FileOutputStream("trips.xlsx")) {
            wb.write(out);
        }

        wb.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {
        new Trippy();
    }

    private TreeSet<Instant> getBinaryPoint(IntervalService service, String pv, Instant begin, Instant end, boolean one) throws SQLException, IOException {
        Metadata metadata = service.findMetadata(pv);
        IntervalQueryParams params = new IntervalQueryParams(metadata, begin, end);

        TreeSet<Instant> set = new TreeSet<>();

        try (IntEventStream stream = service.openIntStream(params)) {

            IntEvent event;

            while ((event = stream.read()) != null) {

                System.out.println(event.getValue());
                
                if ((one && event.getValue() == 1) || (!one && event.getValue() == 0)) {
                    System.out.println("Adding value");
                    set.add(event.getTimestampAsInstant());
                }
            }
        }

        return set;
    }

    private List<TreeSet<Instant>> getBinaryPoint(IntervalService service, String pv, Instant begin, Instant end) throws SQLException, IOException {
        Metadata metadata = service.findMetadata(pv);
        IntervalQueryParams params = new IntervalQueryParams(metadata, begin, end);

        TreeSet<Instant> setOne = new TreeSet<>();
        TreeSet<Instant> setZero = new TreeSet<>();

        try (IntEventStream stream = service.openIntStream(params)) {

            IntEvent event;

            while ((event = stream.read()) != null) {
                if (event.getValue() == 1) {
                    setOne.add(event.getTimestampAsInstant());
                } else if (event.getValue() == 0) {
                    setZero.add(event.getTimestampAsInstant());
                }
            }
        }

        ArrayList<TreeSet<Instant>> list = new ArrayList<>();

        list.add(setZero);
        list.add(setOne);

        return list;
    }

}
