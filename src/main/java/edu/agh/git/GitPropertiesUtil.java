package edu.agh.git;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import static edu.agh.git.PropertiesConstants.*;

public class GitPropertiesUtil {

    private Repository repository;

    public GitPropertiesUtil(Repository repository) {
        this.repository = repository;
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        File dotGitDirectory = new File(".git/");
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository git = repositoryBuilder
                .setGitDir(dotGitDirectory)
                .readEnvironment()
                .findGitDir()
                .build();
        new GitPropertiesUtil(git).prepareGitProperties();
        new DiffMaker(git, System.out).putDiffToStream();
    }

    private Properties prepareGitProperties() throws IOException {
        Properties properties = new Properties();
        properties.put(BRANCH_NAME, repository.getBranch());
        properties.put(AUTHOR_NAME, getAuthorName(repository));
        properties.put(AUTHOR_EMAIL, getEmail(repository));
        properties.put(MESSAGE, getFullMessage(repository));
        properties.put(COMMIT_TIME, getCommitTime(repository));
        return properties;
    }

    private static String getCommitTime(Repository git) throws IOException {
        long time = getRevCommitHead(git).getCommitTime();
        Date date = new Date(time * 1000);
        return date.toString();
    }

    private static String getEmail(Repository git) throws IOException {
        return getRevCommitHead(git).getAuthorIdent().getEmailAddress();
    }

    private static String getFullMessage(Repository git) throws IOException {
        RevCommit headCommit = getRevCommitHead(git);
        return headCommit.getFullMessage();
    }

    private static String getAuthorName(Repository git) throws IOException {
        RevCommit headCommit = getRevCommitHead(git);
        return headCommit.getAuthorIdent().getName();
    }

    public static RevCommit getRevCommitHead(Repository git) throws IOException {
        Ref headRef = git.getRef(Constants.HEAD);
        ObjectId headObjectId = headRef.getObjectId();
        RevWalk revWalk = new RevWalk(git);
        return revWalk.parseCommit(headObjectId);
    }


}
