//package com.shttm.main;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileFilter;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Split the Reuters SGML documents into Simple Text files containing: Title,
// * Date, Dateline, Body
// */
//public class LDAMain {
//  private File reutersDir;
//  private File outputDir;
//  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
//
//  public LDAMain(File reutersDir, File outputDir) {
//    this.reutersDir = reutersDir;
//    this.outputDir = outputDir;
//    System.out.println("Deleting all files in " + outputDir);
//    for (File f : outputDir.listFiles()) {
//      f.delete();
//    }
//  }
//
//  public void extract() {
//    File[] sgmFiles = reutersDir.listFiles(new FileFilter() {
//      public boolean accept(File file) {
//        return file.getName().endsWith(".sgm");
//      }
//    });
//    if (sgmFiles != null && sgmFiles.length > 0) {
//      for (File sgmFile : sgmFiles) {
//        extractFile(sgmFile);
//      }
//    } else {
//      System.err.println("No .sgm files in " + reutersDir);
//    }
//  }
//
//  Pattern EXTRACTION_PATTERN = Pattern
//      .compile("<TITLE>(.*?)|(.*?)|(.*?)");
//
//  private static String[] META_CHARS = { "&", "<", ">", "\"", "'" };
//
//  private static String[] META_CHARS_SERIALIZATIONS = { "&", "<",
//      ">", "\"", "'" };
//
//  /**
//   * Override if you wish to change what is extracted
//   * 
//   * @param sgmFile
//   */
//  protected void extractFile(File sgmFile) {
//    try {
//      BufferedReader reader = new BufferedReader(new FileReader(sgmFile));
//
//      StringBuilder buffer = new StringBuilder(1024);
//      StringBuilder outBuffer = new StringBuilder(1024);
//
//      String line = null;
//      int docNumber = 0;
//      while ((line = reader.readLine()) != null) {
//        // when we see a closing reuters tag, flush the file
//
//        if (line.indexOf("</REUTERS") == -1) {
//          // Replace the SGM escape sequences
//
//          buffer.append(line).append(' ');// accumulate the strings for now,
//                                          // then apply regular expression to
//                                          // get the pieces,
//        } else {
//          // Extract the relevant pieces and write to a file in the output dir
//          Matcher matcher = EXTRACTION_PATTERN.matcher(buffer);
//          while (matcher.find()) {
//            for (int i = 1; i <= matcher.groupCount(); i++) {
//              if (matcher.group(i) != null) {
//                outBuffer.append(matcher.group(i));
//              }
//            }
//            outBuffer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
//          }
//          String out = outBuffer.toString();
//          for (int i = 0; i < META_CHARS_SERIALIZATIONS.length; i++) {
//            out = out.replaceAll(META_CHARS_SERIALIZATIONS[i], META_CHARS[i]);
//          }
//          File outFile = new File(outputDir, sgmFile.getName() + "-"
//              + (docNumber++) + ".txt");
//          // System.out.println("Writing " + outFile);
//          FileWriter writer = new FileWriter(outFile);
//          writer.write(out);
//          writer.close();
//          outBuffer.setLength(0);
//          buffer.setLength(0);
//        }
//      }
//      reader.close();
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  public static void main(String[] args) {
////    if (args.length != 2) {
////      usage("Wrong number of arguments ("+args.length+")");
////      return;
////    }
//    File reutersDir = new File("data/reuters21578/");
//    if (!reutersDir.exists()) {
//      usage("Cannot find Path to Reuters SGM files ("+reutersDir+")");
//      return;
//    }
//    
//    // First, extract to a tmp directory and only if everything succeeds, rename
//    // to output directory.
//    File outputDir = new File("data/reuters21578/qq");
//    outputDir = new File(outputDir.getAbsolutePath() + "-tmp");
//    outputDir.mkdirs();
//    LDAMain extractor = new LDAMain(reutersDir, outputDir);
//    extractor.extract();
//    // Now rename to requested output dir
//    outputDir.renameTo(new File("data/reuters21578/qq"));
//  }
//
//  private static void usage(String msg) {
//    System.err.println("Usage: "+msg+" :: java -cp <...> org.apache.lucene.benchmark.utils.ExtractReuters  ");
//  }
//  
//}