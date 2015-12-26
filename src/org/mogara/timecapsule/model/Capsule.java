package org.mogara.timecapsule.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.mogara.timecapsule.core.Config;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.alibaba.simpleimage.analyze.kdtree.KDTree;
import com.alibaba.simpleimage.analyze.sift.SIFT;
import com.alibaba.simpleimage.analyze.sift.match.Match;
import com.alibaba.simpleimage.analyze.sift.match.MatchKeys;
import com.alibaba.simpleimage.analyze.sift.render.RenderImage;
import com.alibaba.simpleimage.analyze.sift.scale.KDFeaturePoint;

public final class Capsule {

	private static final String ROOT_PATH = Config.STORAGE_PATH + "/capsule";
	private static final String REMOTE_PROCESSOR_IP = "192.168.1.104";
	private static final int REMOTE_PROCESSOR_PORT = 9527;
	private static final int MIN_MATCHED_KEY_NUM = 100;
	private static final int LOCAL_SIFT_IMAGE_PIXEL_LIMIT = 100;

	private String id = null;

	public Capsule() {
		this.id = generateId();
	}

	public Capsule(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public boolean mkdirs() {
		File dir = new File(getPath());
		return dir.mkdirs();
	}

	public String getPath() {
		return ROOT_PATH + "/" + id;
	}

	public String getRawImage() {
		return getPath() + "/hotspot.jpg";
	}

	public String getTextPath() {
		return getPath() + "/intro.txt";
	}

	public String getGalleryPath() {
		return getPath() + "/images";
	}

	public File[] getGalleryFiles() {
		File gallery = new File(getGalleryPath());
		if (gallery.exists() && gallery.isDirectory()) {
			return gallery.listFiles();
		}

		return null;
	}

	public String getVideoPath() {
		return getPath() + "/video.mp4";
	}

	private List<KDFeaturePoint> featurePoints = null;

	public List<KDFeaturePoint> getKDFeaturePoint() {
		if (featurePoints != null) {
			return featurePoints;
		}

		File siftFile = new File(getPath() + "/image.sift");
		if (REMOTE_PROCESSOR_IP != null && !siftFile.exists()) {
			try {
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(REMOTE_PROCESSOR_IP, REMOTE_PROCESSOR_PORT), 3000);
				OutputStream output = socket.getOutputStream();

				File imageFile = new File(getRawImage());

				byte[] length_array = new byte[4];
				int length = (int) imageFile.length();
				length_array[0] = (byte) ((length >>> 24) & 0xFF);
				length_array[1] = (byte) ((length >>> 16) & 0xFF);
				length_array[2] = (byte) ((length >>> 8) & 0xFF);
				length_array[3] = (byte) ((length >>> 0) & 0xFF);
				output.write(length_array);

				InputStream fileInput = new FileInputStream(imageFile);
				byte[] buffer = new byte[1024];
				int offset = 0;
				do {
					int readLength = fileInput.read(buffer);
					if (readLength == -1)
						break;
					output.write(buffer, 0, readLength);
					offset += readLength;
				} while (offset < length);

				fileInput.close();

				InputStream input = socket.getInputStream();

				OutputStream fileOutput = new FileOutputStream(siftFile);
				offset = 0;
				while (true) {
					int readLength = input.read(buffer, 0, buffer.length);
					if (readLength == -1)
						break;
					fileOutput.write(buffer, 0, readLength);
					offset += readLength;
				}
				fileOutput.close();
				socket.close();

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!siftFile.exists()) {
			File imgFile = new File(getRawImage());
			if (!imgFile.exists()) {
				return null;
			}

			Bitmap hotspotBitmap = BitmapFactory.decodeFile(imgFile.getPath());
			featurePoints = calculateKDFeaturePoint(hotspotBitmap);

			try {
				siftFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			try {
				FileOutputStream os = new FileOutputStream(siftFile);
				Iterator<KDFeaturePoint> it = featurePoints.iterator();
				while (it.hasNext()) {
					KDFeaturePoint fp = it.next();
					JSONArray ap = new JSONArray();
					ap.put(fp.dim);
					ap.put(Float.floatToIntBits(fp.x));
					ap.put(Float.floatToIntBits(fp.y));
					ap.put(Float.floatToIntBits(fp.scale));
					ap.put(Float.floatToIntBits(fp.orientation));

					JSONArray desc = new JSONArray();
					for (int i = 0; i < fp.descriptor.length; i++) {
						desc.put(fp.descriptor[i]);
					}
					ap.put(desc);

					os.write(ap.toString().getBytes());
					os.write('\n');
				}
				os.close();
			} catch (IOException e) {
				return null;
			}

		} else {
			featurePoints = new ArrayList<KDFeaturePoint>();

			try {
				BufferedReader br = new BufferedReader(new FileReader(siftFile));
				String line = null;
				while ((line = br.readLine()) != null) {
					JSONArray arr = new JSONArray(line);

					KDFeaturePoint fp = new KDFeaturePoint();
					fp.dim = arr.getInt(0);
					fp.x = Float.intBitsToFloat(arr.getInt(1));
					fp.y = Float.intBitsToFloat(arr.getInt(2));
					fp.scale = Float.intBitsToFloat(arr.getInt(3));
					fp.orientation = Float.intBitsToFloat(arr.getInt(4));

					JSONArray desc = arr.getJSONArray(5);
					fp.descriptor = new int[desc.length()];
					for (int i = 0; i < desc.length(); i++) {
						fp.descriptor[i] = desc.getInt(i);
					}

					featurePoints.add(fp);
				}

				br.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return featurePoints;
	}

	public static String find(Bitmap bitmap) {
		File storage = new File(Capsule.ROOT_PATH);
		if (!storage.exists()) {
			storage.mkdirs();
			return null;
		}

		List<KDFeaturePoint> snapshotFP = calculateKDFeaturePoint(bitmap);
		KDTree snapshotKDTree = KDTree.createKDTree(snapshotFP);
		if (snapshotKDTree == null) {
			return null;
		}

		int matchedKeyNum = MIN_MATCHED_KEY_NUM;
		String targetCapsule = null;
		File[] capsuleDirs = storage.listFiles();
		for (int i = 0; i < capsuleDirs.length; i++) {
			File dir = capsuleDirs[i];
			if (!dir.isDirectory()) {
				continue;
			}

			Capsule capsule = new Capsule(dir.getName());
			List<KDFeaturePoint> spotFP = capsule.getKDFeaturePoint();
			if (spotFP == null) {
				continue;
			}

			List<Match> ms = MatchKeys.findMatchesBBF(spotFP, snapshotKDTree);

			if (ms.size() > matchedKeyNum) {
				matchedKeyNum = ms.size();
				targetCapsule = dir.getName();
			}
		}

		return targetCapsule;
	}

	public static List<KDFeaturePoint> calculateKDFeaturePoint(Bitmap bitmap) {
		RenderImage image = new RenderImage(bitmap);
		image.scaleWithin(LOCAL_SIFT_IMAGE_PIXEL_LIMIT);
		SIFT sift = new SIFT();
		sift.detectFeatures(image.toPixelFloatArray(null));
		return sift.getGlobalKDFeaturePoints();
	}

	protected static String generateId() {
		while (true) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < 16; i++) {
				int ascii = (int) Math.floor(Math.random() * 62);
				if (ascii < 26) {
					buffer.append((char) ('a' + ascii));
				} else if (ascii < 52) {
					buffer.append((char) ('A' + ascii - 26));
				} else {
					buffer.append((char) ('0' + ascii - 52));
				}
			}

			String result = buffer.toString();
			File file = new File(ROOT_PATH + "/" + result);
			if (!file.exists())
				return buffer.toString();
		}
	}
}
