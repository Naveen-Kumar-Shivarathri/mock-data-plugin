package com.oneentropy;


import com.oneentropy.common.Constants;
import com.oneentropy.model.TableConfig;
import com.oneentropy.model.TableMetaData;
import com.oneentropy.model.TableStruct;
import com.oneentropy.util.CommonUtil;
import com.oneentropy.util.FileUtil;
import com.oneentropy.util.SQLGenerator;
import com.oneentropy.util.TableDefinitionParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mojo(name = Constants.GENERATE_SQL_QUERIES, defaultPhase = LifecyclePhase.COMPILE)
public class MockDataPlugin extends AbstractMojo {

    private static final String TABLE_DEF_DIR = "mockData";

    private static final String MOCKED_DATA_DIR = "sql";
    private static final String TABLE_DEF_FILE_REGX = "(.*)\\.(.*)\\.(.*)";
    private static final Pattern PATTERN = Pattern.compile(TABLE_DEF_FILE_REGX);

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    @Parameter(property = "tableConfig", required = true, readonly = true)
    private TableConfig tableConfig;

    private Log logger = getLog();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.info("Running Mock Data Plugin");
        if (CommonUtil.hasContent(tableConfig.getDelimiter()))
            TableDefinitionParser.DELIMITER = tableConfig.getDelimiter();
        logger.info("Table Config:"+tableConfig);
        File basedir = mavenProject.getBasedir();
        String tableDefDirPath = basedir.getAbsolutePath() + File.separator + TABLE_DEF_DIR;
        logger.info("Table Definitions Directory:"+tableDefDirPath);
        File tableDefDir = new File(tableDefDirPath);
        if (!tableDefDir.exists()) {
            throw new MojoExecutionException("Cannot find mockData directory, please add mockData directory and try again");
        }
        File[] files = tableDefDir.listFiles();
        List<TableMetaData> tableMetaData = new LinkedList<>();
        for (File file : files) {
            logger.info("Iterating File:"+file.getAbsolutePath());
            TableMetaData metaData = extractTableMetaData(file);
            logger.info("Table MetaData:"+metaData);
            if (metaData != null) {
                tableMetaData.add(metaData);
            }
        }

        List<TableStruct> tableStructList = TableDefinitionParser.parse(tableMetaData);
        logger.info("Table Structure list:"+tableStructList);
        generateMockData(tableStructList);

    }

    private void generateMockData(List<TableStruct> tableStructList) {
        String basePath = mavenProject.getBasedir().getAbsolutePath();
        basePath = basePath + File.separator +TABLE_DEF_DIR+File.separator+ MOCKED_DATA_DIR + File.separator;
        if(tableConfig.getRecords()==null){
            tableConfig.setRecords(10);
        }
        for (TableStruct tableStruct : tableStructList) {
            logger.info("Generating SQL for:"+tableStruct);
            String insertSql = SQLGenerator.generateMultiInserts(tableStruct, tableConfig.getRecords());
            logger.info("Generated Sql:\n"+insertSql);
            try {
                FileUtil.dumpDataTo(insertSql, basePath + tableStruct.getDatabase() + "." + tableStruct.getTableName() + ".sql");
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

    }

    private TableMetaData extractTableMetaData(File file) {
        if (file.isDirectory())
            return null;
        String name = file.getName();
        logger.info("TableDefinition file:"+name);
        if (tableConfig.getExclusion()!=null&&tableConfig.getExclusion().contains(name))
            return null;
        Matcher matcher = PATTERN.matcher(name);
        if (matcher.matches()) {
            logger.info("TableDefinition file name is valid");
            return TableMetaData.builder().database(matcher.group(1)).tableName(matcher.group(2)).tableDefPath(file.getAbsolutePath()).build();
        }
        return null;
    }


}
