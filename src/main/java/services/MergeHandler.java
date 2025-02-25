package services;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ResolveMerger.MergeFailureReason;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import io.jsonwebtoken.io.IOException;

public class MergeHandler {
	
	static MergeHandler mergeHandler = null;
	
	private MergeHandler() {
		
	}
	
	public static MergeHandler getInstance() {
		
		if(mergeHandler == null) {
			mergeHandler = new MergeHandler();
		}
		
		return mergeHandler;
	}
	
	public Map<String, String> mergeBranches(String repoPath, String targetBranch, String sourceBranch) {
        Map<String, String> result = new HashMap<>();

        try {
            // Open the bare repository
            Repository repository = new FileRepositoryBuilder()
                    .setGitDir(new File(repoPath))
                    .build();

            // Resolve branch references
            ObjectId targetBranchId = repository.resolve("refs/heads/" + targetBranch);
            ObjectId sourceBranchId = repository.resolve("refs/heads/" + sourceBranch);

            if (targetBranchId == null || sourceBranchId == null) {
                throw new IllegalArgumentException("One of the branches does not exist: " + 
                    (targetBranchId == null ? targetBranch : sourceBranch));
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit targetCommit = revWalk.parseCommit(targetBranchId);
                RevCommit sourceCommit = revWalk.parseCommit(sourceBranchId);

                // Step 1: Find the merge base (common ancestor)
                revWalk.reset();
                revWalk.markStart(targetCommit);
                revWalk.markStart(sourceCommit);
                RevCommit baseCommit = revWalk.next(); // Simplified; see note below for full merge base logic

                // Step 2: Perform the in-core merge
                ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(repository, true); // true = in-core
                merger.setBase(baseCommit);
                boolean mergeSuccess = merger.merge(targetCommit, sourceCommit);

                if (!mergeSuccess) {
//                    // Merge failed due to conflicts
//                    List<String> unmergedPaths = merger.getUnmergedPaths();
//                    for (String path : unmergedPaths) {
//                        result.put(path, "CONFLICT");
//                    }
                    System.out.println("Merge conflicts detected in: ");
                } else {
                    // Merge succeeded, create a merge commit
                    ObjectInserter inserter = repository.newObjectInserter();
                    CommitBuilder commitBuilder = new CommitBuilder();
                    commitBuilder.setTreeId(merger.getResultTreeId());
                    commitBuilder.setParentIds(targetBranchId, sourceBranchId);
                    commitBuilder.setAuthor(new PersonIdent("System", "system@example.com"));
                    commitBuilder.setCommitter(new PersonIdent("System", "system@example.com"));
                    commitBuilder.setMessage("Merged " + sourceBranch + " into " + targetBranch);

                    ObjectId mergeCommitId = inserter.insert(commitBuilder);
                    inserter.flush();

                    // Update the target branch reference
                    RefUpdate refUpdate = repository.updateRef("refs/heads/" + targetBranch);
                    refUpdate.setNewObjectId(mergeCommitId);
                    refUpdate.setForceUpdate(false);
                    RefUpdate.Result updateResult = refUpdate.update();

                    if (updateResult != RefUpdate.Result.REJECTED) {
                        result.put("status", "SUCCESS");
                        result.put("mergeCommit", mergeCommitId.getName());
                        System.out.println("Merge completed successfully: " + mergeCommitId.getName());
                    } else {
                        throw new RuntimeException("Failed to update branch reference: " + targetBranch);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Merge error: " + e.getMessage());
            result.put("error", e.getMessage());
        }

        return result;
    }
	public void mergeBranchConflict(String repopath, String targetBranch, String sourceBranch, enums.MergeStrategy mergeStrategy) {
	

		try (Git git= Git.open(new File(repopath))){
			
			Repository repository = git.getRepository();
			
			if(repository.getBranch() != targetBranch) {
				git.checkout().setName(targetBranch).call();
			}
			
			if(mergeStrategy.equals(enums.MergeStrategy.OURS)) {
						git.merge()
			            .include(repository.resolve(sourceBranch))
			            .setStrategy(MergeStrategy.OURS) 
			            .setCommit(true) 
			            .call();
			}
			
			else if(mergeStrategy.equals(enums.MergeStrategy.THEIRS)){
				git.merge()
	            .include(repository.resolve(sourceBranch))
	            .setStrategy(MergeStrategy.THEIRS) // or OURS, THEIRS, etc.
	            .setCommit(true) // Auto-commit if no conflicts
	            .call();
	}
					
			
		
		} catch (Exception e) {
			System.out.println("Merge branches error : "+e.getMessage());
		}
		
	}
	
	public Map<String, int[][]> hasMergeConflict(String repopath, String targetBranch, String sourceBranch){
		Map<String, int[][]> map = new HashMap<String, int[][]>();
		
		try (Git git= Git.open(new File(repopath))){
			
			Repository repository = git.getRepository();
			if(repository.getBranch() != targetBranch) {
				git.checkout().setName(targetBranch).call();
			}
			
			MergeResult mergeResult = git.merge()
		            .include(repository.resolve(sourceBranch))
		            .setStrategy(MergeStrategy.RECURSIVE) 
		            .setFastForward(MergeCommand.FastForwardMode.NO_FF)
		            .setCommit(true) 
		            .call();
			
			if(!mergeResult.getMergeStatus().isSuccessful()) {
				map = mergeResult.getConflicts();
			}
			
		} catch (Exception e) {
			System.out.println("Merge branches error : "+e.getMessage());
		}
		
		return map;	
	}
	
	
	
}
