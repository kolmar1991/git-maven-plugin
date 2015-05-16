package edu.agh.mojo;

import edu.agh.git.DiffMaker;
import edu.agh.git.GitPropertiesUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @goal run
 * @phase process-sources
 */
public class MyMojo extends AbstractMojo {

    /**
     * Diff file location
     * @parameter default-value="/"
     */
    private String diffLocation;

    /**
     * Should generate diff file?
     * @parameter default-value=false
     */
    private boolean addDiffFile;

    /**
     * Diff file name
     * @parameter default-value="diff.patch"
     */
    private String diffFileName;


    /**
     * Project build directory
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String projectDirectory;

    /**
     * Git directory
     * @parameter default-value="${project.basedir}/.git"
     */
    private String gitDirectory;

    /**
     * Contains the full list of projects in the reactor.
     *
     * @parameter property="reactorProjects" default-value="${reactorProjects}"
     */
    private List<MavenProject> reactorProjects;

    public void execute() throws MojoExecutionException {
        getLog().info("Hello  world!");
        try {
            File dotGitDirectory = new File(gitDirectory);
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            Repository git = repositoryBuilder
                    .setGitDir(dotGitDirectory)
                    .readEnvironment()
                    .findGitDir()
                    .build();
            GitPropertiesUtil gitPropertiesUtil = new GitPropertiesUtil(git);
            Properties gitProperties = gitPropertiesUtil.prepareGitProperties();
            appendPropertiesToProjects(gitProperties);
            if (addDiffFile) {
                createDiffFile(git);
            }

        } catch (Exception e) {
            e.printStackTrace(); // perfect pattern
        }
    }

    private void appendPropertiesToProjects(Properties properties) {
        for (MavenProject mavenProject : reactorProjects) {
            Properties mavenProperties = mavenProject.getProperties();
            for (Object key : properties.keySet()) {
                mavenProperties.put(key, properties.get(key));
            }
        }
    }

    public void createDiffFile(Repository git) throws MojoExecutionException {
        File f = createDirIfDoesntExist();
        File diff = new File(f, diffFileName);

        createAndSaveDiff(git, diff);
    }

    private File createDirIfDoesntExist() {
        File f = new File(projectDirectory + "/classes/" + diffLocation);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    private void createAndSaveDiff(Repository git, File diff) throws MojoExecutionException {
        FileOutputStream w = null;
        try {
            w = new FileOutputStream(diff);
            DiffMaker diffMaker = new DiffMaker(git, w);
            diffMaker.putDiffToStream();
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + diff, e);
        } catch (GitAPIException e) {
            throw new MojoExecutionException("Error while creating diff content ", e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }


}
