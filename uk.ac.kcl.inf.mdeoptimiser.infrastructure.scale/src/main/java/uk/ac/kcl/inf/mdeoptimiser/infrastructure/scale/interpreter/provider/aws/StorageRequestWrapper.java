package uk.ac.kcl.inf.mdeoptimiser.infrastructure.scale.interpreter.provider.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

public class StorageRequestWrapper {

  //This is the master bucket in which all the files will be stored
  private final String BUCKET_NAME = "mdeo-scale";

  private BatchParametersProvider batchParametersProvider;
  private AmazonS3 s3Client;
  private TransferManager s3TransferManager;

  public StorageRequestWrapper(BatchParametersProvider batchParametersProvider) {
    this.batchParametersProvider = batchParametersProvider;
    this.createDefaultBucket();
  }

  public StorageRequestWrapper(AmazonS3 s3Client){
    this.s3Client = s3Client;
  }

  /**
   * Creates a default AWS S3 Bucket where all the files will be stored.
   * @return
   */
  public boolean createDefaultBucket() {
    var s3Client = this.batchParametersProvider.getS3Client();

    if(!s3Client.doesBucketExist(this.getDefaultBucketName())){
      s3Client.createBucket(this.getDefaultBucketName());
    }

    return true;
  }

  public String getDefaultBucketName(){
    return this.BUCKET_NAME;
  }

  //TODO Fix prefixes for files with experiment name
  public boolean uploadFiles(Map<String, File> filesList){

    //Uploads the files in the specified KEY locations
    //Key is of the form: datetime-experiment-name/configured paths in spec
    for(var fileKey : filesList.keySet()) {

      var file = filesList.get(fileKey);

      if(file.isDirectory()) {
        this.uploadDirectory(fileKey, file);
      } else {
        this.uploadFile(fileKey, file);
      }

    }

    try {
      this.shutdown();

      return true;

    } catch (Exception e){

    }

    return false;
  }

  /**
   * Download the task files from the specified task file keys to the specified files.
   * @param filesList
   * @return
   */
  public boolean downloadFiles(Map<String, File> filesList) {

    for (var fileKey : filesList.keySet()) {

      var file = filesList.get(fileKey);

      //TODO Can't see any other way of making this check without the thing bein available
      if( !FilenameUtils.getExtension(fileKey).isEmpty()){
        this.downloadFile(fileKey, file);
      } else {
        this.downloadDirectory(fileKey, file);
      }
    }

    return true;
  }

  private void uploadDirectory(String key, File directory){

    try {
      //TODO I believe the API can print command line progress here
      System.out.println(String.format("Uploading %s to s3 %s", directory.getAbsolutePath(), this.getDefaultBucketName()+"/" + key));
      var directoryUpload = this.getFolderTransferClient().uploadDirectory(this.getDefaultBucketName(), key, directory, true);

      while(directoryUpload.isDone() == false){
        System.out.println(String.format("Uploading %s - Progress: %s%%", key, directoryUpload.getProgress().getPercentTransferred()));

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    } catch (AmazonServiceException e) {
      System.err.println(e.getErrorMessage());
      System.exit(1);
    }
  }


  private void uploadFile(String key, File file){

    System.out.println(String.format("Uploading %s to %s", file.getPath(), this.getDefaultBucketName()+"/" + key));

    this.getFileTransferClient().putObject(this.getDefaultBucketName(), key, file);
  }


  /**
   * Download a directory from AWS3 from the specified key to the specified file.
   * @param key
   * @param directory
   */
  private void downloadDirectory(String key, File directory) {

    try {
      System.out.println(String.format("Downloading %s from s3 to %s", directory.getAbsolutePath(), this.getDefaultBucketName()+"/" + key));

      var directoryDownload = this.getFolderTransferClient().downloadDirectory(this.getDefaultBucketName(), key, new File("./"), true);

      while(directoryDownload.isDone() == false){
        System.out.println(String.format("Downloading %s - Progress: %s%%", key, directoryDownload.getProgress().getPercentTransferred()));
        Thread.sleep(1000);
      }

      Files.move(Paths.get(key), directory.toPath());

    } catch (AmazonServiceException | IOException e) {
      e.printStackTrace();
      System.exit(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Download a file from AWS3 from the specified key to the specified file.
   * @param key
   * @param file
   */
  private void downloadFile(String key, File file){

    try {

      if (file.getParentFile() != null && !file.getParentFile().exists()){
        file.getParentFile().mkdirs();
      }

      System.out.println(String.format("Downloading %s to %s", this.getDefaultBucketName() + "/" + key, file.getPath()));
      S3Object s3File  = this.getFileTransferClient().getObject(this.getDefaultBucketName(), key);

      Files.copy(s3File.getObjectContent(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

    } catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private AmazonS3 getFileTransferClient(){

    if(this.s3Client == null) {
      this.s3Client = this.batchParametersProvider.getS3Client();
    }

    return this.s3Client;

  }

  private TransferManager getFolderTransferClient() {

    if(this.s3TransferManager == null){
      this.s3TransferManager = TransferManagerBuilder.standard().build();
    }

    return this.s3TransferManager;
  }

  public void shutdown() {
    this.getFileTransferClient().shutdown();
    this.getFolderTransferClient().shutdownNow(true);
  }
}
