package services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;

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
	
	public Map<String, int[][]> mergeBranches(String repopath, String targetBranch, String sourceBranch) {
		
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
