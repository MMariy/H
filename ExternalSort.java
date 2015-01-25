
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Deflater;

public class ExternalSort {
        
        public static void sort(final File input, final File output) throws IOException {
                ExternalSort.mergeSortedFiles(ExternalSort.sortInBatch(input),output);
        }

        public static final int DEFAULTMAXTEMPFILES = 1024;

        public static long estimateBestSizeOfBlocks(final long sizeoffile,
                final int maxtmpfiles) {
        
        		long blocksize = sizeoffile / maxtmpfiles
                        + (sizeoffile % maxtmpfiles == 0 ? 0 : 1);

                long freemem = Runtime.getRuntime().freeMemory();
                if (blocksize < freemem / 2) {
                        blocksize = freemem / 2;
                }
                return blocksize;
        }


       
        public static List<File> sortInBatch(File file)
                throws IOException {
                return sortInBatch(file, defaultcomparator, DEFAULTMAXTEMPFILES,
                        Charset.defaultCharset(), null, false);
        }
        
        public static List<File> sortInBatch(File file, Comparator<String> cmp)
                throws IOException {
                return sortInBatch(file, cmp, DEFAULTMAXTEMPFILES,
                        Charset.defaultCharset(), null, false);
        }
        public static List<File> sortInBatch(File file, Comparator<String> cmp,
                boolean distinct) throws IOException {
                return sortInBatch(file, cmp, DEFAULTMAXTEMPFILES,
                        Charset.defaultCharset(), null, distinct);
        }

        
        public static List<File> sortInBatch(File file, Comparator<String> cmp,
                int maxtmpfiles, Charset cs, File tmpdirectory,
                boolean distinct, int numHeader, boolean usegzip)
                throws IOException {
                BufferedReader fbr = new BufferedReader(new InputStreamReader(
                        new FileInputStream(file), cs));
                return sortInBatch(fbr,file.length(),  cmp,
                        maxtmpfiles, cs, tmpdirectory,
                        distinct, numHeader, usegzip);
        }
        
        public static List<File> sortInBatch(final BufferedReader fbr, final long datalength) throws IOException {
                return sortInBatch(fbr,datalength, defaultcomparator, DEFAULTMAXTEMPFILES,
                        Charset.defaultCharset(), null, false,0,false);
        }
        
        public static List<File> sortInBatch(final BufferedReader fbr, final long datalength, final Comparator<String> cmp,
                final boolean distinct) throws IOException {
                return sortInBatch(fbr,datalength, cmp, DEFAULTMAXTEMPFILES,
                        Charset.defaultCharset(), null, distinct,0,false);
        }
 
       
        public static List<File> sortInBatch(final BufferedReader fbr, final long datalength, final Comparator<String> cmp,
                final int maxtmpfiles, final Charset cs, final File tmpdirectory,
                final boolean distinct, final int numHeader, final boolean usegzip)
                throws IOException {
                List<File> files = new ArrayList<File>();
                long blocksize = estimateBestSizeOfBlocks(datalength, maxtmpfiles);// in
                                                                             // bytes

                try {
                        List<String> tmplist = new ArrayList<String>();
                        String line = "";
                        try {
                                int counter = 0;
                                while (line != null) {
                                        long currentblocksize = 0;// in bytes
                                        while ((currentblocksize < blocksize)
                                                && ((line = fbr.readLine()) != null)) {
                                                // as  long as  you have enough memory
                                                if (counter < numHeader) {
                                                        counter++;
                                                        continue;
                                                }
                                                tmplist.add(line);
                                                currentblocksize += StringSizeEstimator
                                                        .estimatedSizeOf(line);
                                        }
                                        files.add(sortAndSave(tmplist, cmp, cs,
                                                tmpdirectory, distinct, usegzip));
                                        tmplist.clear();
                                }
                        } catch (EOFException oef) {
                                if (tmplist.size() > 0) {
                                        files.add(sortAndSave(tmplist, cmp, cs,
                                                tmpdirectory, distinct, usegzip));
                                        tmplist.clear();
                                }
                        }
                } finally {
                        fbr.close();
                }
                return files;
        }
        
        
        public static List<File> sortInBatch(File file, Comparator<String> cmp,
                int maxtmpfiles, Charset cs, File tmpdirectory, boolean distinct)
                throws IOException {
                return sortInBatch(file, cmp, maxtmpfiles, cs, tmpdirectory,
                        distinct, 0, false);
        }

        
        public static File sortAndSave(List<String> tmplist,
                Comparator<String> cmp, Charset cs, File tmpdirectory,
                boolean distinct, boolean usegzip) throws IOException {
                Collections.sort(tmplist, cmp);
                File newtmpfile = File.createTempFile("sortInBatch",
                        "flatfile", tmpdirectory);
                newtmpfile.deleteOnExit();
                OutputStream out = new FileOutputStream(newtmpfile);
                int ZIPBUFFERSIZE = 2048;
                if (usegzip)
                        out = new GZIPOutputStream(out, ZIPBUFFERSIZE) {
                                {
                                        this.def.setLevel(Deflater.BEST_SPEED);
                                }
                        };
                BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(
                        out, cs));
                String lastLine = null;
                try {
                        for (String r : tmplist) {
                                // Skip duplicate lines
                                if (!distinct || !r.equals(lastLine)) {
                                        fbw.write(r);
                                        fbw.newLine();
                                        lastLine = r;
                                }
                        }
                } finally {
                        fbw.close();
                }
                return newtmpfile;
        }

        
        public static File sortAndSave(List<String> tmplist,
                Comparator<String> cmp, Charset cs, File tmpdirectory)
                throws IOException {
                return sortAndSave(tmplist, cmp, cs, tmpdirectory, false, false);
        }
        
        public static int mergeSortedFiles(List<File> files, File outputfile) throws IOException {
                return mergeSortedFiles(files, outputfile, defaultcomparator,
                        Charset.defaultCharset());
        }
        
        public static int mergeSortedFiles(List<File> files, File outputfile,
                final Comparator<String> cmp) throws IOException {
                return mergeSortedFiles(files, outputfile, cmp,
                        Charset.defaultCharset());
        }

        
        public static int mergeSortedFiles(List<File> files, File outputfile,
                final Comparator<String> cmp, boolean distinct)
                throws IOException {
                return mergeSortedFiles(files, outputfile, cmp,
                        Charset.defaultCharset(), distinct);
        }

        
        public static int mergeSortedFiles(List<File> files, File outputfile,
                final Comparator<String> cmp, Charset cs, boolean distinct,
                boolean append, boolean usegzip) throws IOException {
                ArrayList<BinaryFileBuffer> bfbs = new ArrayList<BinaryFileBuffer>();
                for (File f : files) {
                        final int BUFFERSIZE = 2048;
                        InputStream in = new FileInputStream(f);
                        BufferedReader br;
                        if (usegzip) {
                                br = new BufferedReader(new InputStreamReader(
                                        new GZIPInputStream(in, BUFFERSIZE), cs));
                        } else {
                                br = new BufferedReader(new InputStreamReader(in,
                                        cs));
                        }

                        BinaryFileBuffer bfb = new BinaryFileBuffer(br);
                        bfbs.add(bfb);
                }
                BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outputfile, append), cs));
                int rowcounter = mergeSortedFiles(fbw,cmp,distinct, bfbs);
                for (File f : files) f.delete();
                return rowcounter;
        }

        public static int mergeSortedFiles(BufferedWriter fbw, final Comparator<String> cmp, 
                boolean distinct, List<BinaryFileBuffer> buffers) throws IOException {
                PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(
                        11, new Comparator<BinaryFileBuffer>() {
                                @Override
                                public int compare(BinaryFileBuffer i,
                                        BinaryFileBuffer j) {
                                        return cmp.compare(i.peek(), j.peek());
                                }
                        });
                for (BinaryFileBuffer bfb: buffers)
                        if(!bfb.empty())
                                pq.add(bfb);
                int rowcounter = 0;
                String lastLine = null;
                try {
                        while (pq.size() > 0) {
                                BinaryFileBuffer bfb = pq.poll();
                                String r = bfb.pop();
                                // Skip duplicate lines
                                if (!distinct || !r.equals(lastLine)) {
                                        fbw.write(r);
                                        fbw.newLine();
                                        lastLine = r;
                                }
                                ++rowcounter;
                                if (bfb.empty()) {
                                        bfb.fbr.close();
                                } else {
                                        pq.add(bfb); // add it back
                                }
                        }
                } finally {
                        fbw.close();
                        for (BinaryFileBuffer bfb : pq)
                                bfb.close();
                }
                return rowcounter;

        }

      
        public static int mergeSortedFiles(List<File> files, File outputfile,
                final Comparator<String> cmp, Charset cs, boolean distinct)
                throws IOException {
                return mergeSortedFiles(files, outputfile, cmp, cs, distinct,
                        false, false);
        }

        public static int mergeSortedFiles(List<File> files, File outputfile,
                final Comparator<String> cmp, Charset cs) throws IOException {
                return mergeSortedFiles(files, outputfile, cmp, cs, false);
        }

        
        public static Comparator<String> defaultcomparator = new Comparator<String>() {
                @Override
				public int compare(String o1, String o2) {
					// TODO Auto-generated method stub
                	int a = Integer.parseInt(o1);
                	int b = Integer.parseInt(o2);
					if(a==b){
                		return 0;
                	}
                	else if(a<b){
                		return -1;
                	}
                	else {
                		return 1;
                	}
				}
        };

}



final class BinaryFileBuffer {
        public BufferedReader fbr;
        private String cache;

        public BinaryFileBuffer(BufferedReader r)
                throws IOException {
                this.fbr = r;
                reload();
        }

        public boolean empty() {
                return this.cache == null;
        }

        private void reload() throws IOException {
                this.cache = this.fbr.readLine();
        }

        public void close() throws IOException {
                this.fbr.close();
        }

        public String peek() {
                return this.cache;
        }

        public String pop() throws IOException {
        	
        	
        	
                String answer = peek().toString();// make a copy
                reload();
                return answer;
                
               
        }

}
