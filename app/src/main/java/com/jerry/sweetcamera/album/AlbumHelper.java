package com.jerry.sweetcamera.album;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;



import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Thumbnails.IMAGE_ID与Media._ID对应
 * @author Administrator
 *
 */
public class AlbumHelper {
	final String TAG = getClass().getSimpleName();
	
	Context context;
	ContentResolver cr;
	HashMap<String, String> thumbnailList = new HashMap<String,String>();
	List<HashMap<String, String>> albumList = new ArrayList<HashMap<String,String>>();
	List<ImageItem> mImagesList = new ArrayList<ImageItem>();
	HashMap<String, ImageBucket> bucketList = new HashMap<String, ImageBucket>();
	
	private static AlbumHelper instance;
	private AlbumHelper(){
	}
	public static AlbumHelper getHelper(){
		if(instance == null){
			instance = new AlbumHelper();
		}
		return instance;
	}
	public void init(Context context){
		if(this.context == null){
			this.context = context;
			cr = context.getContentResolver();
		}
	}
		
	private void getThumbnail(){
		Cursor cursor = null;
		try {
			String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA };
			cursor = cr.query(Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
			getThumbnailColumnData(cursor);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
	void getAlbum(){
		Cursor cursor = null;
		try {
			String[] projection = { Albums._ID, Albums.ALBUM, Albums.ALBUM_ART, Albums.ALBUM_KEY, Albums.ARTIST, Albums.NUMBER_OF_SONGS };
			cursor = cr.query(Albums.EXTERNAL_CONTENT_URI, projection, null, null, null);
			getAlbumColumnData(cursor);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
	boolean hasBuildImagesBucketList = false;
	void buildImagesBucketList(){

		Cursor cur = null;

		try {
			long startTime = System.currentTimeMillis();

			//构造缩略图索引
			getThumbnail();

			//构造相册索引
			String columns[] = new String[] { Media._ID, Media.BUCKET_ID/*, Media.PICASA_ID*/, Media.DATA/*, Media.DISPLAY_NAME*/
					/*, Media.TITLE, Media.SIZE*/, Media.BUCKET_DISPLAY_NAME/*, Media.DATE_MODIFIED*/};
			// 得到一个游标
			cur = cr.query(Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, Media.DATE_MODIFIED + " DESC");
			if (cur.moveToFirst()) {
				// 获取指定列的索引
				int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
				int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
//				int photoNameIndex = cur.getColumnIndexOrThrow(Media.DISPLAY_NAME);
//				int photoTitleIndex = cur.getColumnIndexOrThrow(Media.TITLE);
//				int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
				int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
				int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
//				int picasaIdIndex = cur.getColumnIndexOrThrow(Media.PICASA_ID);
//				int modifiedDataIndex = cur.getColumnIndexOrThrow(Media.DATE_MODIFIED);
				// 获取图片总数
//				int totalNum = cur.getCount();

				do {
					String _id = cur.getString(photoIDIndex);
//					String name = cur.getString(photoNameIndex);
		            String path = cur.getString(photoPathIndex);
//		            String title = cur.getString(photoTitleIndex);
//		            String size = cur.getString(photoSizeIndex);
		            String bucketName = cur.getString(bucketDisplayNameIndex);
		            String bucketId = cur.getString(bucketIdIndex);
//		            String picasaId = cur.getString(picasaIdIndex);
//		            String modifiedData = cur.getString(modifiedDataIndex);

//		            Log.i(TAG, _id + ", bucketId: "+bucketId+", picasaId: "+picasaId + " name:" + name + " path:"
//		                    + path+" title: "+title+" size: "+size+" bucket: "+bucketName+ " modifiedData: " + modifiedData +"---");
		            File tmp = new File(path);
//		            if (tmp != null && tmp.exists() && tmp.length() > 100) {
		            	ImageBucket bucket = bucketList.get(bucketId);
			            if(bucket == null){
			            	bucket = new ImageBucket();
			            	bucketList.put(bucketId, bucket);
			            	bucket.imageList = new ArrayList<ImageItem>();
			            	bucket.bucketName = bucketName;
			            }
			            bucket.count++;
			            ImageItem imageItem = new ImageItem();
			            imageItem.imageId = _id;
			            imageItem.imagePath = path;
			            imageItem.thumbnailPath = thumbnailList.get(_id);
			            bucket.imageList.add(imageItem);
//		            } else {
////		            	Log.d(TAG, "getImagesBucket faile file do not exist : " + path);
//		            }

		        } while (cur.moveToNext());
			}

//			Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet().iterator();
//			while (itr.hasNext()) {
//				Map.Entry<String, ImageBucket> entry = (Map.Entry<String, ImageBucket>) itr.next();
//				ImageBucket bucket = entry.getValue();
//				Log.d(TAG, entry.getKey()+", "+bucket.bucketName+", "+bucket.count+" ---------- ");
//				for(int i=0; i<bucket.imageList.size(); ++i){
//					ImageItem image = bucket.imageList.get(i);
//					Log.d(TAG, "----- "+image.imageId+", "+image.imagePath+", "+image.thumbnailPath);
//				}
//			}
			hasBuildImagesBucketList = true;
			long endTime = System.currentTimeMillis();
			Log.d(TAG, "use time: "+(endTime - startTime)+" ms");
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (cur != null && !cur.isClosed()) {
				cur.close();
			}
		}
	}


	/**
	 * 获取图库全部图片
	 * */
	List<ImageItem> buildImagesList(){

		Cursor cur = null;

		try {
			long startTime = System.currentTimeMillis();

			//构造缩略图索引
//			getThumbnail();

			mImagesList.clear();

			//构造相册索引
//			String columns[] = new String[] { Media._ID, Media.BUCKET_ID, Media.PICASA_ID, Media.DATA, Media.DISPLAY_NAME,
//					Media.TITLE, Media.SIZE, Media.BUCKET_DISPLAY_NAME, Media.DATE_MODIFIED };
			String columns[] = new String[] {Media._ID, Media.DATA};
			// 得到一个游标
			cur = cr.query(Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, Media.DATE_MODIFIED + " DESC");
			if (cur.moveToFirst()) {
				// 获取指定列的索引
				int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
				int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
//				int photoNameIndex = cur.getColumnIndexOrThrow(Media.DISPLAY_NAME);
//				int photoTitleIndex = cur.getColumnIndexOrThrow(Media.TITLE);
//				int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
//				int bucketDisplayNameIndex = cur.getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);
//				int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
//				int picasaIdIndex = cur.getColumnIndexOrThrow(Media.PICASA_ID);
//				int modifiedDataIndex = cur.getColumnIndexOrThrow(Media.DATE_MODIFIED);
//				// 获取图片总数
//				int totalNum = cur.getCount();

				do {
					String _id = cur.getString(photoIDIndex);
					String path = cur.getString(photoPathIndex);

//					String name = cur.getString(photoNameIndex);
//		            String title = cur.getString(photoTitleIndex);
//		            String size = cur.getString(photoSizeIndex);
//		            String bucketName = cur.getString(bucketDisplayNameIndex);
//		            String bucketId = cur.getString(bucketIdIndex);
//		            String picasaId = cur.getString(picasaIdIndex);
//		            String modifiedData = cur.getString(modifiedDataIndex);

//		            Log.i(TAG, _id + ", bucketId: "+bucketId+", picasaId: "+picasaId + " name:" + name + " path:"
//		                    + path+" title: "+title+" size: "+size+" bucket: "+bucketName+ " modifiedData: " + modifiedData +"---");

					File tmp = new File(path);
//					if (tmp != null && tmp.exists() && tmp.length() > 100) {

		            	ImageItem imageItem = new ImageItem();
			            imageItem.imageId = _id;
			            imageItem.imagePath = path;
			            imageItem.thumbnailPath = thumbnailList.get(_id);
			            mImagesList.add(imageItem);

//		            } else {
////		            	Log.d(TAG, "getAllImages faile file do not exist : " + path);
//		            }



		        } while (cur.moveToNext());
			}

			long endTime = System.currentTimeMillis();
			Log.d(TAG, "get album image list use time: "+(endTime - startTime)+" ms");

			return mImagesList;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (cur != null && !cur.isClosed()) {
				cur.close();
			}
		}
		return mImagesList;
	}


	public List<ImageBucket> getImagesBucketList(boolean refresh){
		if(refresh || (!refresh && !hasBuildImagesBucketList)){
			thumbnailList.clear();
			albumList.clear();
			bucketList.clear();
			buildImagesBucketList();
		}

		List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
		Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, ImageBucket> entry = (Map.Entry<String, ImageBucket>) itr.next();
			tmpList.add(entry.getValue());
		}
		return tmpList;
	}
	
	public List<ImageItem> getImagesList(){
//		if(refresh){
//			thumbnailList.clear();
//			albumList.clear();
//			bucketList.clear();
////			buildImagesBucketList();
//		}

		return buildImagesList();
	}

	String getOriginalImagePath(String image_id) {
		String path = null;
		Cursor cursor = null;
		try {
			Log.i(TAG, "---(^o^)----" + image_id);
			String[] projection = { Media._ID, Media.DATA };
			cursor = cr.query(Media.EXTERNAL_CONTENT_URI, projection,
					Media._ID + "=" + image_id, null, null);
			if (cursor != null) {
				cursor.moveToFirst();
				path = cursor.getString(cursor.getColumnIndex(Media.DATA));

			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		return path;
	}

	private void getThumbnailColumnData(Cursor cur) {
	    if (cur.moveToFirst()) {
//	        int _id;
	        int image_id;
	        String image_path;
//	        int _idColumn = cur.getColumnIndex(Thumbnails._ID);
	        int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
	        int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

	        do {
	            // Get the field values
//	            _id = cur.getInt(_idColumn);
	            image_id = cur.getInt(image_idColumn);
	            image_path = cur.getString(dataColumn);

	            // Do something with the values.
//	            Log.i(TAG, _id + " image_id:" + image_id + " path:" + image_path + "---");
//	            HashMap<String, String> hash = new HashMap<String, String>();
//	            hash.put("image_id", image_id + "");
//	            hash.put("path", image_path);
//	            thumbnailList.add(hash);
	            thumbnailList.put(""+image_id, image_path);
//	            File tmp = new File(image_path);
//	            if (tmp.exists()) {
//	            	thumbnailList.put(""+image_id, image_path);
//	            } else {
//	            	Log.d(TAG, "getThumbnail faile file do not exist : " + image_path);
//	            }
	            
	        } while (cur.moveToNext());
	     }
	}
	
	private void getAlbumColumnData(Cursor cur) {
	    if (cur.moveToFirst()) {
	        int _id;
	        String album;
	        String albumArt;
	        String albumKey;
	        String artist;
	        int numOfSongs;
	        
	        int _idColumn = cur.getColumnIndex(Albums._ID);
	        int albumColumn = cur.getColumnIndex(Albums.ALBUM);
	        int albumArtColumn = cur.getColumnIndex(Albums.ALBUM_ART);
	        int albumKeyColumn = cur.getColumnIndex(Albums.ALBUM_KEY);
	        int artistColumn = cur.getColumnIndex(Albums.ARTIST);
	        int numOfSongsColumn = cur.getColumnIndex(Albums.NUMBER_OF_SONGS);

	        do {
	            // Get the field values
	            _id = cur.getInt(_idColumn);
	            album = cur.getString(albumColumn);
	            albumArt = cur.getString(albumArtColumn);
	            albumKey = cur.getString(albumKeyColumn);
	            artist = cur.getString(artistColumn);
	            numOfSongs = cur.getInt(numOfSongsColumn);

	            // Do something with the values.
//	            Log.i(TAG, _id + " album:" + album + " albumArt:"
//	                        + albumArt + "albumKey: "+ albumKey+" artist: "+artist+" numOfSongs: "+numOfSongs+"---");
	            HashMap<String, String> hash = new HashMap<String, String>();
	            hash.put("_id", _id + "");
	            hash.put("album", album);
	            hash.put("albumArt", albumArt);
	            hash.put("albumKey", albumKey);
	            hash.put("artist", artist);
	            hash.put("numOfSongs", numOfSongs+"");
	            albumList.add(hash);

	        } while (cur.moveToNext());

	     }
	}
	
	public boolean saveImg(Bitmap bmp, String savePath){
		return utils.saveImg(bmp, savePath);
	}
	/**
	 * 通知系统媒体库扫描指定路径
	 * @param imgPath
	 */
	public void refreshSystemAlbum(String picPath){
		utils.refreshSystemAlbum(picPath);
	}
	Utils utils = new Utils();
	class Utils{
		public boolean saveImg(Bitmap bmp, String savePath){
			if(TextUtils.isEmpty(savePath)){
				return false;
			}
			
			boolean saveOk = false;
			try {
//				String uri = MediaStore.Images.Media.insertImage(getContentResolver(), path, "name", "description");
//				String uri = MediaStore.Images.Media.insertImage(getContentResolver(), bmp, title, "description");//用这2种方式插入系统相册，那么可以用getFilePathByContentResolver得到图片路径
				
				saveOk = bmp.compress(CompressFormat.JPEG, 80, new FileOutputStream(new File(savePath)));
				if(saveOk){
					refreshSystemAlbum(savePath);
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			return saveOk;
		}
		
		public void refreshSystemAlbum(String picPath){
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri uri = Uri.fromFile(new File(picPath));
			intent.setData(uri); 
			context.sendBroadcast(intent);
		}
	
		/**
		 * 调用 MediaStore.Images.Media.insertImage方法将图片插入系统相册后，根据返回的uri得到图片路径
		 * @param context
		 * @param uri
		 * @return
		 */
		private String getFilePathByContentResolver(Context context, Uri uri) {
			Cursor c = null;
			String filePath = null;
			try {
				if (null == uri) {
					return null;
				}
				c = cr.query(uri, null, null, null,
						null);
				filePath = null;
				if (null == c) {
					throw new IllegalArgumentException("Query on " + uri
							+ " returns null result.");
				}
				try {
					if ((c.getCount() != 1) || !c.moveToFirst()) {
					} else {
						filePath = c.getString(c
								.getColumnIndexOrThrow(MediaColumns.DATA));
					}
				} finally {
					c.close();
				}
				return filePath;
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			} finally {
				if (c != null && !c.isClosed()) {
					c.close();
				}
			}
			
			return filePath;
		}
	}

}
