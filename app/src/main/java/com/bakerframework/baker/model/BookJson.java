/**
 * Copyright (c) 2013-2014. Francisco Contreras, Holland Salazar.
 * Copyright (c) 2015. Tobias Strebitzer, Francisco Contreras, Holland Salazar.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 * Neither the name of the Baker Framework nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package com.bakerframework.baker.model;

import com.admag.AdmagSDK;
import com.bakerframework.baker.BakerApplication;
import com.bakerframework.baker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookJson {
	private String hpub;
	private String magazineName;
	private String title;
	private List<String> authors;
    private List<String> creators;
	private Date date;
	private String url;
	private String cover;
	private String orientation;
	private boolean zoomable;
	private String background;
	private boolean verticalBounce;
	private int indexHeight;
	private boolean mediaDisplay;
	private String pageNumberColors;
	private String rendering;
	private boolean pageTurnTap;
    private String liveUrl;
	private List<String> contents;

	public BookJson() {
		this.hpub = "1";
		this.date = new Date();
		this.authors = new ArrayList<>();
		this.creators = new ArrayList<>();
		this.contents = new ArrayList<>();
		this.title = "";
		this.url = "";
		this.cover = "";
		this.orientation = "";
		this.zoomable = false;
		this.background = "";
		this.verticalBounce = false;
		this.indexHeight = 0;
		this.mediaDisplay = false;
		this.pageNumberColors = "";
		this.rendering = "";
		this.pageTurnTap = false;
	}

	public String getHpub() {
		return hpub;
	}

	public void setHpub(String hpub) {
		this.hpub = hpub;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public boolean isZoomable() {
		return zoomable;
	}

	public void setZoomable(boolean zoomable) {
		this.zoomable = zoomable;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public boolean isVerticalBounce() {
		return verticalBounce;
	}

	public void setVerticalBounce(boolean verticalBounce) {
		this.verticalBounce = verticalBounce;
	}

	public int getIndexHeight() {
		return indexHeight;
	}

	public void setIndexHeight(int indexHeight) {
		this.indexHeight = indexHeight;
	}

	public boolean isMediaDisplay() {
		return mediaDisplay;
	}

	public void setMediaDisplay(boolean mediaDisplay) {
		this.mediaDisplay = mediaDisplay;
	}

	public String getPageNumberColors() {
		return pageNumberColors;
	}

	public void setPageNumberColors(String pageNumberColors) {
		this.pageNumberColors = pageNumberColors;
	}

	public String getRendering() {
		return rendering;
	}

	public void setRendering(String rendering) {
		this.rendering = rendering;
	}

	public boolean isPageTurnTap() {
		return pageTurnTap;
	}

	public void setPageTurnTap(boolean pageTurnTap) {
		this.pageTurnTap = pageTurnTap;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	public List<String> getCreators() {
		return creators;
	}

	public void setCreators(List<String> creators) {
		this.creators = creators;
	}

	public List<String> getContents() {
		return contents;
	}

	public void setContents(List<String> contents) {
		this.contents = contents;
	}

	public String getMagazineName() {
		return magazineName;
	}

	public void setIssueName(String magazineName) {
		this.magazineName = magazineName;
	}

    public String getLiveUrl() {
        return liveUrl;
    }

    public void setLiveUrl(String liveUrl) {
        this.liveUrl = liveUrl;
    }

    public void fromJsonString(final String jsonString) throws JSONException, ParseException {
        this.fromJson(new JSONObject(jsonString));
    }

    public boolean fromIssue(Issue issue) {

        JSONObject jsonObject = issue.getBookJsonObject();

        // Validate book json
        try {
            this.validateJson(jsonObject);
            this.fromJson(jsonObject);
        } catch (JSONException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        this.setIssueName(issue.getName());

        return true;
    }

    public void fromJson(JSONObject json) throws JSONException {
        if (json.has("liveUrl")) {
            this.liveUrl = json.getString("liveUrl");
        }

		// The other properties are commented by now, as we are not gonna use them yet.
		this.hpub = json.optString("hpub", "1");
		this.title = json.optString("title", "");
		this.url = json.optString("url", "");
		this.cover = json.optString("cover", "");
        this.orientation = json.optString("orientation", "PORTRAIT");

        // Parse contents
        this.contents = new ArrayList<>();
        JSONArray contents = new JSONArray(json.getString("contents"));


        List<Integer> listPagesAds = AdmagSDK.getPages(Integer.parseInt(BakerApplication.getInstance().getString(
                R.string.admag_publication_id)),
                json.getString("title"));

		boolean contentsAlreadyHaveAd = false;
		for (int i = 0; i < contents.length(); i++) {
			if (contents.getString(i).equals("pageHaveAd")) {
				contentsAlreadyHaveAd = true;
				break;
			}
		}

		int positionAd = 0;

		for (int i = 0; i < contents.length(); i++) {
			if (!contentsAlreadyHaveAd && listPagesAds.contains(i + 1 + positionAd)) {
				this.contents.add("pageHaveAd");
				positionAd+=1;
			}

			this.contents.add(contents.getString(i));
		}
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd",
                Locale.US);

        result.put("hpub", this.hpub);
        result.put("title", this.title);
        result.put("date", sdfInput.format(this.date));
        result.put("url", this.url);
        result.put("cover", this.cover);
        result.put("orientation", this.orientation);
        result.put("zoomable", this.zoomable);
        result.put("-baker-background", this.background);
        result.put("-baker-vertical-bounce", this.verticalBounce);
        result.put("-baker-index-height", this.indexHeight);
        result.put("-baker-media-autoplay", this.mediaDisplay);
        result.put("-baker-page-numbers-color", this.pageNumberColors);
        result.put("-baker-rendering", this.rendering);
        result.put("-baker-page-turn-tap", this.pageTurnTap);
        result.put("liveUrl", this.liveUrl);

		JSONArray authors = new JSONArray();
		JSONArray creators = new JSONArray();
		JSONArray contents = new JSONArray();

		for (String author : this.authors) {
			authors.put(author);
		}
		result.put("author", authors);

		for (String creator : this.creators) {
			creators.put(creator);
		}
		result.put("creator", creators);

		for (String content : this.contents) {
			contents.put(content);
		}
		result.put("contents", contents);

		return result;
	}

    private void validateJson(final JSONObject json) throws Exception {
        for (String property : getRequiredProperties()) {
            if (!json.has(property)) {
                throw new MissingPropertyException(property);
            }
        }
        JSONArray contents = new JSONArray(json.getString("contents"));
        if (contents.length() < 0) {
            throw new MissingContentException();
        }
    }

    private String[] getRequiredProperties() {
        return new String[]{"contents"};
    }

    private class MissingPropertyException extends Exception {
        private final String property;

        public MissingPropertyException(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    private class MissingContentException extends Exception {

    }
}
