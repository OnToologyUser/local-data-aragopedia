package com.localidata.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.localidata.process.bean.ConfigBean;
import com.localidata.process.bean.DataBean;
import com.localidata.process.bean.SkosBean;
import com.localidata.util.Constants;
import com.localidata.util.Prop;
import com.localidata.util.Utils;

/**
 * 
 * @author Localidata
 */
public class GenerateData {
	private final static Logger log = Logger.getLogger(GenerateData.class);
	private String inputDirectoryString = "";
	private String outputDirectoryString = "";
	private String configDirectoryString = "";
	private String[] extensions = new String[] { "csv" };
	private String[] extensionsConfig = new String[] { "xlsx", "csv" };
	private HashMap<String, ConfigBean> mapconfig = new HashMap<String, ConfigBean>();
	private ArrayList<DataBean> dataWithSkos = new ArrayList<DataBean>();
	private ArrayList<DataBean> dataWithSkosHierarchical = new ArrayList<DataBean>();
	private ArrayList<String> dsdList = new ArrayList<String>();
	private ArrayList<String> propertiesList = new ArrayList<String>();

	public GenerateData(String input, String output, String config) {
		this.inputDirectoryString = input;
		this.outputDirectoryString = output;
		this.configDirectoryString = config;
	}

	private void readConfig() {
		log.debug("Init readConfig");
		log.info("Comienza a extraerse la configuración");
		File configDirectoryFile = new File(configDirectoryString);
		File areasReportFile = new File(outputDirectoryString + File.separator
				+ "areas.txt");
		Collection<File> listCSV = FileUtils.listFiles(configDirectoryFile,
				extensionsConfig, true);
		int cont = 0;
		int size = listCSV.size();
		for (File file : listCSV) {
			log.info("Se extrae el fichero " + file.getName() + " " + (++cont)
					+ " " + size);
			if (!file.getName().startsWith("mapping")) {
				ConfigBean configBean = new ConfigBean();
				configBean.setNameFile(file.getName());
				String id = file.getName().substring(8);
				String areas = "";
				id = id.replace(".csv", "");
				id = id.replace(".xlsx", "");
				if (id.contains("TC")) {
					id = id.replace("TC", "");
					configBean.getLetters().add("TC");
					areas += "TC ";
				}
				if (id.contains("TM")) {
					id = id.replace("TM", "");
					configBean.getLetters().add("TM");
					areas += "TM ";
				}
				if (id.contains("TP")) {
					id = id.replace("TP", "");
					configBean.getLetters().add("TP");
					areas += "TP ";
				}
				if (id.contains("A")) {
					id = id.replace("A", "");
					configBean.getLetters().add("A");
					areas += "A ";
				}
				while (id.charAt(id.length() - 1) == '-') {
					id = id.substring(0, id.length() - 1);
				}

				configBean.setId(id);
				 if (Prop.formatConfig.equals("csv")) {
					 readCsv(file, configBean);
				 } else {
					 readXlsxFile(file, configBean);
				 }
				mapconfig.put(id, configBean);
				Utils.stringToFileAppend(id + " " + areas + "\n",
						areasReportFile);
			}
		}

		for (Iterator<DataBean> it1 = dataWithSkosHierarchical.iterator(); it1.hasNext();) {
			DataBean data1 = (DataBean) it1.next();
			if(data1.getRelationKos()!=null){
				DataBean data2 = mapconfig.get(data1.getIdConfig()).getMapData().get(data1.getRelationKos());
				HashMap<String, SkosBean> mapSkos = data1.mergeSkos(data2);
				if(mapSkos!=null){
					log.info("Kos "+data1.getName()+" is parent of "+data2.getName());
					data2.setWriteSkos(false);
					data1.setMapSkos(mapSkos);
					data2.setMapSkos(mapSkos);
					mapconfig.get(data1.getIdConfig()).getMapData().get(data1.getNameNormalized()).setMapSkos(mapSkos);
					mapconfig.get(data2.getIdConfig()).getMapData().get(data2.getNameNormalized()).setMapSkos(mapSkos);
				}
			}
		}
		log.info("Finaliza de extraerse la configuración");
		log.debug("End readConfig");
	}

	private void readCsv(File file, ConfigBean configBean) {
		log.debug("Init readCsv");
		List<String> csvLines;
		try {
			csvLines = FileUtils.readLines(file, "UTF-8");

			String[] cellsName = csvLines.get(0).split(",");
			String[] cellsNameNormalized = csvLines.get(1).split(",");
			String[] cellsNormalization = csvLines.get(2).split(",");
			String[] cellsDimMesure = csvLines.get(3).split(",");
			String[] cellsType = csvLines.get(4).split(",");
			String[] cellsSkosfile = csvLines.get(5).split(",");
			String[] cellsConstant=null;
			String[] cellsConstantValue=null;
			String[] cellsRelationKos=null;
			String[] cellsKosName=null;

			if(csvLines.size()==7)
				cellsConstant = csvLines.get(6).split(",");
			if(csvLines.size()==8)
				cellsConstantValue = csvLines.get(7).split(",");
			if(csvLines.size()==9)
				cellsRelationKos = csvLines.get(9).split(",");
			if(csvLines.size()==10)
				cellsKosName = csvLines.get(10).split(",");
			
			
			int columnReaded = 0;
			while (columnReaded < cellsName.length) {
				
				DataBean dataBean = new DataBean();
				if (cellsName[columnReaded] == null) {
						columnReaded++;
				} else {
					dataBean.setName(removeStartEndCaracter(cellsName[columnReaded]));
					dataBean.setNameNormalized(removeStartEndCaracter(cellsNameNormalized[columnReaded]));
					dataBean.setNormalizacion(removeStartEndCaracter(cellsNormalization[columnReaded]));
					dataBean.setDimensionMesure(removeStartEndCaracter(cellsDimMesure[columnReaded]));
					dataBean.setIdConfig(configBean.getId());
					String type = "";
					if (Utils.v(removeStartEndCaracter(cellsType[columnReaded]))) {
						type = removeStartEndCaracter(cellsType[columnReaded]);
					} else {
						type = "xsd:string";
					}
					dataBean.setType(type);
					if (Utils.v(removeStartEndCaracter(cellsSkosfile[columnReaded]))) {
						HashMap<String, SkosBean> mapSkos = readMappingFileCSV(removeStartEndCaracter(cellsSkosfile[columnReaded]));
						dataBean.setMapSkos(mapSkos);
						configBean.getMapData().put(dataBean.getNameNormalized(),
								dataBean);
						dataWithSkos.add(dataBean);
					} else {
						configBean.getMapData().put(dataBean.getNameNormalized(),
								dataBean);
					}
					if (Prop.addDataConstant 
							&& cellsConstant!=null
							&& Utils.v(removeStartEndCaracter(cellsConstant[columnReaded]))
							&& removeStartEndCaracter(cellsConstant[columnReaded]).equals(
									Constants.constante)) {
						if (Utils.v(removeStartEndCaracter(cellsConstantValue[columnReaded]))) {
							dataBean.setConstant(removeStartEndCaracter(cellsConstantValue[columnReaded]) + "");
							configBean.getListDataConstant().add(dataBean);
						}
					}
					if (cellsRelationKos!=null && Utils.v(removeStartEndCaracter(cellsRelationKos[columnReaded]))) {
						dataBean.setRelationKos(removeStartEndCaracter(cellsRelationKos[columnReaded]));
						dataWithSkosHierarchical.add(dataBean);
					}
					if (cellsKosName!=null && Utils.v(removeStartEndCaracter(cellsKosName[columnReaded]))) {
						dataBean.setKosName(removeStartEndCaracter(cellsKosName[columnReaded]));
					} else {
						dataBean.setKosName(dataBean.getNameNormalized());
					}

					columnReaded++;
				}
			}
		} catch (IOException e) {
			log.error("Error read csv ", e);
		}
		log.debug("End readCsv");
	}
	
	private String removeStartEndCaracter(String csvLine) {
		log.debug("Init removeStartEndCaracter");
		String line = csvLine;
		if (csvLine != null) {
			if (csvLine.startsWith("\""))
				line = csvLine.substring(1, csvLine.length());
			if (csvLine.endsWith("\""))
				line = line.substring(0, line.length() - 1);
		}
		log.debug("End removeStartEndCaracter");
		return line;
	}

	private void readXlsxFile(File file, ConfigBean configBean) {
		log.debug("Init readXlsxFile");
		InputStream inp = null;
		Workbook wb = null;
		try {
			inp = new FileInputStream(file);
			wb = WorkbookFactory.create(inp);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (InvalidFormatException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		Sheet sheet = wb.getSheetAt(0);
		Row rowName = sheet.getRow(0);
		Row rowNameNormalized = sheet.getRow(1);
		Row rowNormalization = sheet.getRow(2);
		Row rowDimMesure = sheet.getRow(3);
		Row rowType = sheet.getRow(4);
		Row rowSkosfile = sheet.getRow(5);
		Row rowConstant = sheet.getRow(6);
		Row rowConstantValue = sheet.getRow(7);
		Row rowRelationKos = sheet.getRow(8);
		Row rowKosName = sheet.getRow(9);
		boolean cont = true;
		int columnReaded = 0;
		while (cont) {
			Cell cellName = rowName.getCell(columnReaded);
			Cell cellNameNormalized = rowNameNormalized.getCell(columnReaded);
			Cell cellNormalization = rowNormalization.getCell(columnReaded);
			Cell cellDimMesure = rowDimMesure.getCell(columnReaded);
			Cell cellType = rowType.getCell(columnReaded);
			Cell cellSkosfile = null;
			if (rowSkosfile != null)
				cellSkosfile = rowSkosfile.getCell(columnReaded);
			Cell cellConstant = null;
			if (rowConstant != null)
				cellConstant = rowConstant.getCell(columnReaded);
			Cell cellConstantValue = null;
			if (rowConstantValue != null)
				cellConstantValue = rowConstantValue.getCell(columnReaded);
			Cell cellRelationKos = null;
			if (rowRelationKos != null)
				cellRelationKos = rowRelationKos.getCell(columnReaded);
			Cell cellKosName = null;
			if (rowKosName != null)
				cellKosName = rowKosName.getCell(columnReaded);
			

			DataBean dataBean = new DataBean();
			if (cellName == null) {
				if (rowName.getCell((columnReaded + 1)) == null)
					cont = false;
				else
					columnReaded++;
			} else {
				dataBean.setName(cellName.getStringCellValue());
				dataBean.setNameNormalized(cellNameNormalized
						.getStringCellValue());
				dataBean.setNormalizacion(cellNormalization
						.getStringCellValue());
				dataBean.setDimensionMesure(cellDimMesure.getStringCellValue());
				dataBean.setIdConfig(configBean.getId());
				String type = "";
				if (cellType != null) {
					type = cellType.getStringCellValue();
				} else {
					type = "xsd:string";
				}
				dataBean.setType(type);
				if (cellSkosfile != null
						&& !cellSkosfile.getStringCellValue().equals("")) {
					HashMap<String, SkosBean> mapSkos = readMappingFile(cellSkosfile
							.getStringCellValue());
					dataBean.setMapSkos(mapSkos);
					configBean.getMapData().put(dataBean.getNameNormalized(),
							dataBean);	
					dataWithSkos.add(dataBean);
				} else {
					configBean.getMapData().put(dataBean.getNameNormalized(),
							dataBean);
				}
				if (Prop.addDataConstant
						&& cellConstant != null
						&& cellConstant.getStringCellValue().equals(
								Constants.constante)) {
					if (cellConstantValue != null) {
						dataBean.setConstant(cellConstantValue
								.getStringCellValue() + "");
						configBean.getListDataConstant().add(dataBean);
					}
				}
				if (cellRelationKos != null) {
					dataBean.setRelationKos(cellRelationKos.getStringCellValue());
					dataWithSkosHierarchical.add(dataBean);
				}
				if (cellKosName != null) {
					dataBean.setKosName(cellKosName.getStringCellValue());
				} else {
					dataBean.setKosName(dataBean.getNameNormalized());
				}

				columnReaded++;
			}
		}
		log.debug("End readXlsxFile");
	}

	private HashMap<String, SkosBean> readMappingFile(String skosPath) {
		log.debug("Init readSkosFile");
		HashMap<String, SkosBean> mapSkos = new HashMap<String, SkosBean>();

		File skosMappingg = new File(configDirectoryString + File.separator
				+ skosPath);
		InputStream inp = null;
		Workbook wb = null;
		try {
			inp = new FileInputStream(skosMappingg);
			wb = WorkbookFactory.create(inp);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (InvalidFormatException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		if (wb != null) {
			Sheet sheet = wb.getSheetAt(0);
			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				Row row = sheet.getRow(i);
				Cell cellId = row.getCell(0);
				Cell cellUri = row.getCell(1);
				SkosBean skosBean = new SkosBean();
				SkosBean skosBeanExtra = new SkosBean();
				String idCell = "";
				if (cellId.getCellType() == 0) {
					Double d = new Double(cellId.getNumericCellValue());
					idCell = d.intValue() + "";
				} else {
					idCell = cellId.getStringCellValue();
				}
				skosBean.setLabel(idCell);
				idCell = Utils.urlify(idCell);
				skosBean.setId(idCell);
				String uriCell = "";
				if (cellUri.getCellType() == 0) {
					Double d = new Double(cellUri.getNumericCellValue());
					uriCell = d.intValue() + "";
				} else {
					uriCell = cellUri.getStringCellValue();
					String id = uriCell.substring(uriCell.lastIndexOf("/") + 1,
							uriCell.length());
					if (!idCell.equals(id)) {
						skosBeanExtra.setId(id);
						skosBeanExtra.setLabel(id);
						skosBeanExtra.setURI(uriCell);
						mapSkos.put(id, skosBeanExtra);
					}
				}
				skosBean.setURI(uriCell);
				mapSkos.put(idCell, skosBean);
			}
		}
		log.debug("End readSkosFile");
		return mapSkos;
	}
	
	private HashMap<String, SkosBean> readMappingFileCSV(String skosPath) {
		log.debug("Init readMappingFileCSV");
		HashMap<String, SkosBean> mapSkos = new HashMap<String, SkosBean>();
		
		if(skosPath.endsWith("xlsx"))
			skosPath=skosPath.replace("xlsx", "csv");
		File skosMappingg = new File(configDirectoryString + File.separator
				+ skosPath);
		List<String> csvLines;
		try {
			csvLines = FileUtils.readLines(skosMappingg, "UTF-8");

			for (String line : csvLines) {
				String[] cells = line.split("\",\"");
				String cellId = removeStartEndCaracter(cells[0]);
				String cellUri = removeStartEndCaracter(cells[1]);
				
				SkosBean skosBean = new SkosBean();
				SkosBean skosBeanExtra = new SkosBean();
				skosBean.setLabel(cellId);
				cellId = Utils.urlify(cellId);
				skosBean.setId(cellId);

				String id = cellUri.substring(cellUri.lastIndexOf("/") + 1,
						cellUri.length());
				if (!cellId.equals(id)) {
					skosBeanExtra.setId(id);
					skosBeanExtra.setLabel(id);
					skosBeanExtra.setURI(cellUri);
					mapSkos.put(id, skosBeanExtra);
				}
				skosBean.setURI(cellUri);
				mapSkos.put(cellId, skosBean);
			}
		} catch (IOException e) {
			log.error("Error read csv ", e);
		}
		
		log.debug("End readMappingFileCSV");
		return mapSkos;
	}

	private void writeSkosTTL() {
		log.debug("Init createSkos");
		log.info("Init to create skos");
		File kosFile = new File(outputDirectoryString + File.separator
				+ "DatosTTL" + File.separator + "codelists" + File.separator
				+ "kos.ttl");
		StringBuffer resultIni = new StringBuffer();
		StringBuffer resultFin = new StringBuffer();
		ArrayList<String> kosCreated = new ArrayList<String>();
		resultIni.append(TransformToRDF.addPrefix());

		for (Iterator<DataBean> itDataBean = dataWithSkos.iterator(); itDataBean
				.hasNext();) {

			DataBean dataBean = itDataBean.next();
			if (dataBean != null
					&& !kosCreated.contains(dataBean.getNameNormalized())
					&& dataBean.getMapSkos().size() > 0) {
				String suject = Prop.host + "/" + Prop.kosName + "/"
						+ Prop.datasetName + "/" + dataBean.getKosName();
				resultIni.append("<" + suject + "> "
						+ "a skos:ConceptScheme;\n");
				resultIni.append("\tskos:notation \""
						+ dataBean.getNameNormalized() + "\";\n");
				resultIni.append("\trdfs:label \"" + dataBean.getName()
						+ "\";\n");

				for (Iterator<String> iterator3 = dataBean.getMapSkos()
						.keySet().iterator(); iterator3.hasNext();) {
					String keySkos = iterator3.next();
					SkosBean skosBean = dataBean.getMapSkos().get(keySkos);
					if (skosBean != null) {
						String sujectKos = suject + "/"
								+ Utils.urlify(skosBean.getId());
						if (skosBean.getParent() == null) {
							resultIni.append("\tskos:hasTopConcept <"
									+ sujectKos + ">;\n");
						}
						resultFin.append("<" + sujectKos
								+ "> a skos:Concept;\n");
						resultFin.append("\tskos:inScheme <" + suject + ">;\n");
						String label = skosBean.getId();
						if (skosBean.getLabel() != null
								&& !skosBean.getLabel().equals(""))
							label = skosBean.getLabel();
						resultFin.append("\tskos:notation \""
								+ skosBean.getId() + "\";\n");
						resultFin.append("\tskos:prefLabel \""
								+ Utils.prefLabelClean(label) + "\"");
						if (skosBean.getSons().size() > 0) {
							resultFin.append(";\n");
							for (Iterator<SkosBean> itSons = skosBean.getSons()
									.iterator(); itSons.hasNext();) {
								SkosBean son = itSons.next();
								resultFin.append("\tskos:narrower <" + suject
										+ "/" + son.getId() + ">");
								if (itSons.hasNext()) {
									resultFin.append(";\n");
								} else {
									resultFin.append(".\n");
								}
							}
						} else {
							resultFin.append(".\n");
						}

						resultFin.append("\n");
					}
				}
				resultIni.append("\n");
				resultIni.append(resultFin);
				kosCreated.add(dataBean.getNameNormalized());
			}
			Utils.stringToFileAppend(resultIni.toString(), kosFile);
			resultIni.setLength(0);
			resultFin.setLength(0);
		}

		log.info("end to create skos");
		log.debug("End createSkos");
	}

	private void writeInformationTTL() {
		log.debug("Init extractInformation");
		File inputDirectoryFile = new File(inputDirectoryString);
		File propertiesFile = new File(outputDirectoryString + File.separator
				+ "DatosTTL" + File.separator + "codelists" + File.separator
				+ "properties.ttl");
		File dsdFile = new File(outputDirectoryString + File.separator
				+ "DatosTTL" + File.separator + "dataStructures"
				+ File.separator + "dsd.ttl");
		File errorReportFile = new File(outputDirectoryString + File.separator
				+ "errorReport.txt");

		TransformToRDF.propertiesContent.append(TransformToRDF.addPrefix());
		Utils.stringToFileAppend(TransformToRDF.addPrefix().toString(), dsdFile);

		Collection<File> listCSV = FileUtils.listFiles(inputDirectoryFile,
				extensions, true);
		int numfile = 1;
		for (File file : listCSV) {
			try {
				String fileName = "";
				String fileLetter = "";
				if (file.getName().endsWith("A.csv")) {
					fileName = file.getName().substring(0,
							file.getName().length() - 5);
					fileLetter = file.getName().substring(
							file.getName().length() - 5,
							file.getName().length() - 4);
				} else {
					fileName = file.getName().substring(0,
							file.getName().length() - 6);
					fileLetter = file.getName().substring(
							file.getName().length() - 6,
							file.getName().length() - 4);
				}
				ConfigBean configBean = mapconfig.get(fileName);
				if (configBean != null) {
					File outputDirectoryFile = new File(outputDirectoryString
							+ File.separator + "DatosTTL" + File.separator
							+ "informes" + File.separator + fileName
							+ fileLetter + ".ttl");
					log.info("Init file " + fileName + fileLetter + ". Size "
							+ FileUtils.sizeOf(file) + " " + numfile + "/"
							+ listCSV.size());
					List<String> csvLines = FileUtils.readLines(file, "UTF-8");
					TransformToRDF transformToRDF = new TransformToRDF(
							csvLines, outputDirectoryFile, propertiesFile,
							dsdFile, errorReportFile, configBean);
					transformToRDF.initTransformation(fileName + fileLetter,
							numfile, fileName, dsdList, propertiesList);
					log.info("End file " + outputDirectoryFile.getName() + " "
							+ numfile + "/" + listCSV.size());
				} else {
					log.error("Error al extraer la configuración de "
							+ fileName);
				}
				numfile++;
			} catch (Exception e) {
				log.error("Error al extraer la información ", e);
			}
		}

		log.debug("End extractInformation");
	}

	private void backup() {
		log.debug("Init backup");
		log.info("Comienza a hacerse el backup");
		File outputDirectoryFile = new File(outputDirectoryString);
		if (outputDirectoryFile.exists()) {
			SimpleDateFormat formatFullDate = new SimpleDateFormat("yyyyMMdd");
			String copy = outputDirectoryString + "_"
					+ formatFullDate.format(new Date());
			File copyDirectoryFile = new File(copy);
			int aux = 1;
			while (copyDirectoryFile.exists()) {
				copyDirectoryFile = new File(copy + "_" + aux++);
			}

			try {
				FileUtils.moveDirectoryToDirectory(outputDirectoryFile,
						copyDirectoryFile, true);
			} catch (IOException e) {
				log.error("Error haciendo backup", e);
			}
		}
		log.info("Finaliza de hacerse el backup");
		log.debug("End backup");
	}

	public static void main(String[] args) {
		if ((log == null) || (log.getLevel() == null))
			PropertyConfigurator.configure("log4j.properties");
		if (args.length == 4) {
			log.info("Start process");
			Prop.loadConf();
			GenerateData app = new GenerateData(args[1], args[2], args[3]);
			app.backup();
			app.readConfig();
			app.writeInformationTTL();
			app.writeSkosTTL();
			log.info("Finish process");
		} 

	}
}
