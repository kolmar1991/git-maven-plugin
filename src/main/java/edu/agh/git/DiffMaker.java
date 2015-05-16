package edu.agh.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class DiffMaker {

    private Repository repository;
    private OutputStream stream;

    public DiffMaker(Repository repository, OutputStream stream) {
        this.repository = repository;
        this.stream = stream;
    }

    public void putDiffToStream() throws IOException, GitAPIException {
        AbstractTreeIterator oldTreeParser = prepareTreeParser(getRevCommitBeforeHead());
        AbstractTreeIterator newTreeParser = prepareTreeParser(GitPropertiesUtil.getRevCommitHead(repository));

        List<DiffEntry> diff = new Git(repository).diff().
                setOldTree(oldTreeParser).
                setNewTree(newTreeParser).
                call();
        for (DiffEntry entry : diff) {
            String header = "Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId();
            stream.write(header.getBytes());
            DiffFormatter formatter = new DiffFormatter(stream);
            formatter.setRepository(repository);
            formatter.format(entry);
        }
    }

    private RevCommit getRevCommitBeforeHead() throws IOException {
        String objectId = GitPropertiesUtil.getRevCommitHead(repository).getParent(0).getName();
        RevWalk walk = new RevWalk(repository);
        return walk.parseCommit(ObjectId.fromString(objectId));
    }


    private AbstractTreeIterator prepareTreeParser(RevCommit commit) throws IOException {
        RevWalk walk = new RevWalk(repository);
        RevTree tree = walk.parseTree(commit.getTree().getId());

        CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
        ObjectReader oldReader = repository.newObjectReader();
        try {
            oldTreeParser.reset(oldReader, tree.getId());
        } finally {
            oldReader.release();
        }

        walk.dispose();

        return oldTreeParser;
    }
}