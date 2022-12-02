package com.oneentropy;

import com.oneentropy.common.Strategy;
import com.oneentropy.model.Column;
import com.oneentropy.model.TableConfig;
import com.oneentropy.model.TableStruct;
import com.oneentropy.util.SQLGenerator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Test {

    public static void main(String args[]){

        System.out.println("Strategy:"+ Strategy.valueOf("CONSTANT"));
    }



}
