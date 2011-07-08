package com.googlecode.htmlcompressor.compressor;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Class that stores HTML compression statistics
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 * @see HtmlCompressor#getStatistics()
 */
public class HtmlCompressorStatistics {
	
	private HtmlMetrics originalMetrics = new HtmlMetrics();
	private HtmlMetrics compressedMetrics = new HtmlMetrics();
	private long time = 0;
	private int preservedSize = 0;
	
	/**
	 * Returns metrics of an uncompressed document
	 * 
	 * @return metrics of an uncompressed document
	 * @see HtmlMetrics
	 */
	public HtmlMetrics getOriginalMetrics() {
		return originalMetrics;
	}
	
	/**
	 * @param originalMetrics the originalMetrics to set
	 */
	public void setOriginalMetrics(HtmlMetrics originalMetrics) {
		this.originalMetrics = originalMetrics;
	}
	
	/**
	 * Returns metrics of a compressed document
	 * 
	 * @return metrics of a compressed document
	 * @see HtmlMetrics
	 */
	public HtmlMetrics getCompressedMetrics() {
		return compressedMetrics;
	}
	
	/**
	 * @param compressedMetrics the compressedMetrics to set
	 */
	public void setCompressedMetrics(HtmlMetrics compressedMetrics) {
		this.compressedMetrics = compressedMetrics;
	}
	
	/**
	 * Returns total compression time. 
	 * 
	 * <p>Please note that compression performance varies very significantly depending on whether it was 
	 * a cold run or not (specifics of Java VM), so for accurate real world results it is recommended 
	 * to take measurements accordingly.   
	 * 
	 * @return the compression time, in milliseconds 
	 *      
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	
	/**
	 * Returns total size of blocks that were skipped by the compressor 
	 * (for example content inside <code>&lt;pre></code> tags or inside   
	 * <code>&lt;script></code> tags with disabled javascript compression)
	 * 
	 * @return the total size of blocks that were skipped by the compressor, in bytes
	 */
	public int getPreservedSize() {
		return preservedSize;
	}
	
	/**
	 * @param preservedSize the preservedSize to set
	 */
	public void setPreservedSize(int preservedSize) {
		this.preservedSize = preservedSize;
	}

	@Override
	public String toString() {
		return String.format("Time=%d, Preserved=%d, Original={%s}, Compressed={%s}", time, preservedSize, originalMetrics.toString(), compressedMetrics.toString());
	}
}
