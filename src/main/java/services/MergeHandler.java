package services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.merge.ThreeWayMerger;

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
	
	public boolean detectConflicts(String repoPath, String targetBranch, String sourceBranch) {
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(new File(repoPath))
                .build()) {
            ObjectId targetBranchId = repository.resolve("refs/heads/" + targetBranch);
            ObjectId sourceBranchId = repository.resolve("refs/heads/" + sourceBranch);

            if (targetBranchId == null || sourceBranchId == null) {
                throw new IllegalArgumentException("Branch not found");
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit targetCommit = revWalk.parseCommit(targetBranchId);
                RevCommit sourceCommit = revWalk.parseCommit(sourceBranchId);
                
                revWalk.reset();  
                revWalk.markStart(targetCommit);
                revWalk.markStart(sourceCommit);
                RevCommit baseCommit = revWalk.next();

                if (baseCommit == null) {
                    throw new RuntimeException("No common ancestor found");
                }

                ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(repository, true);
                merger.setBase(baseCommit);
                return !merger.merge(targetCommit, sourceCommit); // true = conflicts, false = clean
            }
        } catch (Exception e) {
            System.err.println("Conflict detection error: " + e.getMessage());
            return true; // Assume conflict on error
        }
    }

	public boolean mergeBranches(String repoPath, String targetBranch, String sourceBranch, String strategy) {
	    boolean isMerged = false;
	    
	    try (Repository repository = new FileRepositoryBuilder()
	            .setGitDir(new File(repoPath))
	            .build()) {
	        ObjectId targetBranchId = repository.resolve("refs/heads/" + targetBranch);
	        ObjectId sourceBranchId = repository.resolve("refs/heads/" + sourceBranch);

	        if (targetBranchId == null || sourceBranchId == null) {
	            throw new IllegalArgumentException("Branch not found");
	        }

	        try (RevWalk revWalk = new RevWalk(repository)) {
	            RevCommit targetCommit = revWalk.parseCommit(targetBranchId);
	            RevCommit sourceCommit = revWalk.parseCommit(sourceBranchId);
	            
	            // Removed incorrect base commit logic
	            ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(repository, true);
	            boolean mergeSuccess = merger.merge(targetCommit, sourceCommit);

	            ObjectId resultTreeId;
	            if (mergeSuccess) {
	                resultTreeId = merger.getResultTreeId();
	            } else if ("OURS".equalsIgnoreCase(strategy)) {
	                resultTreeId = targetCommit.getTree();
	                mergeSuccess = true;
	            } else if ("THEIRS".equalsIgnoreCase(strategy)) {
	                resultTreeId = sourceCommit.getTree();
	                mergeSuccess = true;
	            } else {
	                return false;
	            }

	            // Create merge commit
	            ObjectInserter inserter = repository.newObjectInserter();
	            CommitBuilder commitBuilder = new CommitBuilder();
	            commitBuilder.setTreeId(resultTreeId);
	            commitBuilder.setParentIds(targetBranchId, sourceBranchId);
	            commitBuilder.setAuthor(new PersonIdent("System", "system@example.com"));
	            commitBuilder.setCommitter(new PersonIdent("System", "system@example.com"));
	            commitBuilder.setMessage("Merged " + sourceBranch + " into " + targetBranch + " with " + strategy);

	            ObjectId mergeCommitId = inserter.insert(commitBuilder);
	            inserter.flush();

	            // Update target branch
	            RefUpdate refUpdate = repository.updateRef("refs/heads/" + targetBranch);
	            refUpdate.setNewObjectId(mergeCommitId);
	            RefUpdate.Result updateResult = refUpdate.update();

	            if (updateResult == RefUpdate.Result.REJECTED) {
	                throw new RuntimeException("Failed to update " + targetBranch);
	            }

	            isMerged = true;
	        }
	    } catch (Exception e) {
	        System.err.println("Merge error: " + e.getMessage());
	    }

	    return isMerged;
	}    
	public Map<String, int[][]> mergeBranchesForNonBare(String repopath, String targetBranch, String sourceBranch) {
		
		Map<String, int[][]> map = new HashMap<String, int[][]>();
		
		try (Git git= Git.open(new File(repopath))){
			
			Repository repository = git.getRepository();
			if(repository.getBranch() != targetBranch) {
				git.checkout().setName(targetBranch).call();
			}
			
			MergeResult mergeResult = git.merge()
		            .include(repository.resolve(sourceBranch))
		            .setStrategy(MergeStrategy.RECURSIVE) 
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
