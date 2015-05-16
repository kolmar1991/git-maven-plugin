package edu.agh.mojo;

import edu.agh.git.GitPropertiesUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
public class MyMojo extends AbstractMojo {

    /**
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

}
