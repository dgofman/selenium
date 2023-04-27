package com.softigent.selenium.core.test;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;

public class TestParser {
	
	public static final ConfigurationSource NULL_SOURCE = ConfigurationSource.NULL_SOURCE;
	
	public static CSVParser parseCSV(String content) throws IOException {
		 return CSVParser.parse(content, CSVFormat.DEFAULT);
	}
	
	public static PDDocument paserPDF(RandomAccessRead source) throws IOException {
		PDFParser pdfParser = new PDFParser(source);
		pdfParser.parse();
		return new PDDocument(pdfParser.getDocument());
	}

	public static Workbook parseExcel(InputStream is) throws Exception {
		if (!"2.11.0".equals(IOUtils.class.getPackage().getImplementationVersion())) {
			throw new Exception("XSSFWorkbook required to use org.apache.commons.io version 2.11.0.");
		}
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(is);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		return workbook;
	}
}
