package services;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import models.Branch;

public class RepositoryManager {
	private static String BASE_URL = "/opt/repo/";

	private static File createFileObject(String repoName) {
		File file = new File(BASE_URL + repoName);
		if (file.exists()) {
			return file;
		}
		return null;
	}

	public static void getAllBranches(String repoName) {
		File file = null;

		ArrayList<Branch> outBranches = new ArrayList<>();
		try {
			file = createFileObject(repoName);
		} catch (Exception e) {

			e.printStackTrace();
		}
		if (file != null) {
			try (Git git = Git.open(file)) {
				List<Ref> branches = git.branchList().call(); // Get local branches
				System.out.println("Local Branches:");
				for (Ref branch : branches) {
					System.out.println(branch.getName());

					 try (// Find the first commit of the branch
					RevWalk revWalk = new RevWalk(git.getRepository())) {
						RevCommit firstCommit = revWalk.parseCommit(branch.getObjectId());

						// Get commit timestamp
						int timestamp = firstCommit.getCommitTime(); // Epoch seconds
						LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
						System.out.println("Created At: " + dt);

						revWalk.dispose();
					}
				}

				// Get remote branches
				List<Ref> remoteBranches = git.branchList()
						.setListMode(org.eclipse.jgit.api.ListBranchCommand.ListMode.REMOTE).call();
				System.out.println("\nRemote Branches:");
				for (Ref remoteBranch : remoteBranches) {
					System.out.println(remoteBranch.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void getAllCommits(String repoName) {
		File file = null;
		try {
			file = createFileObject(repoName);
		} catch (Exception e) {

			e.printStackTrace();
		}
		if (file != null) {
			try (Git git = Git.open(file)) {
				Iterable<RevCommit> commits = git.log().call();
				for (RevCommit commit : commits) {
					System.out.println("Commit: " + commit.getName());
					System.out.println("Author: " + commit.getAuthorIdent().getName());
					System.out.println("Date: " + commit.getAuthorIdent().getWhen());
					System.out.println("Message: " + commit.getFullMessage());
					System.out.println("------------------------------");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void getAllFiles(String repoName) {

		File file = null;
		try {
			file = createFileObject(repoName);
		} catch (Exception e) {

			e.printStackTrace();
		}
		if(file!=null) {
		try (Git git = Git.open(file)) {
			Repository repository = git.getRepository();
			ObjectId head = repository.resolve("HEAD^{tree}"); // Get the latest commit tree

			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(head);
				treeWalk.setRecursive(true); // Set to false to get only top-level directories/files

				System.out.println("Repository Files:");
				while (treeWalk.next()) {
					System.out.println(treeWalk.getPathString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
	}
}
