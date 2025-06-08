package com.brouken.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

/**
 * Adapter for displaying network video links and local files in the library
 */
public class LibraryAdapter extends BaseAdapter {
    
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_LOCAL_FILE = 1;
    public static final int TYPE_NETWORK_LINK = 2;
    
    private final Context context;
    private final LayoutInflater inflater;
    private final List<Object> items; // Mixed list of headers, File objects, and NetworkVideoLink objects
    private final OnItemClickListener onItemClickListener;
    private final OnItemLongClickListener onItemLongClickListener;
    
    public interface OnItemClickListener {
        void onLocalFileClick(File file);
        void onNetworkLinkClick(NetworkVideoLink link);
        void onHeaderClick(String headerText);
    }
    
    public interface OnItemLongClickListener {
        boolean onLocalFileLongClick(File file);
        boolean onNetworkLinkLongClick(NetworkVideoLink link);
    }
    
    public LibraryAdapter(Context context, List<Object> items, 
                         OnItemClickListener clickListener, 
                         OnItemLongClickListener longClickListener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
        this.onItemClickListener = clickListener;
        this.onItemLongClickListener = longClickListener;
    }
    
    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getViewTypeCount() {
        return 3;
    }
    
    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            return TYPE_HEADER;
        } else if (item instanceof File) {
            return TYPE_LOCAL_FILE;
        } else if (item instanceof NetworkVideoLink) {
            return TYPE_NETWORK_LINK;
        }
        return TYPE_HEADER;
    }
    
    @Override
    public boolean isEnabled(int position) {
        // Headers are not clickable
        return getItemViewType(position) != TYPE_HEADER;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        View view = convertView;
        
        switch (viewType) {
            case TYPE_HEADER:
                view = createHeaderView(position, view, parent);
                break;
            case TYPE_LOCAL_FILE:
                view = createLocalFileView(position, view, parent);
                break;
            case TYPE_NETWORK_LINK:
                view = createNetworkLinkView(position, view, parent);
                break;
        }
        
        return view;
    }
    
    private View createHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.preference_category, parent, false);
            holder = new HeaderViewHolder();
            holder.titleText = convertView.findViewById(android.R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        
        String headerText = (String) items.get(position);
        holder.titleText.setText(headerText);
        
        convertView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onHeaderClick(headerText);
            }
        });
        
        return convertView;
    }
    
    private View createLocalFileView(int position, View convertView, ViewGroup parent) {
        LocalFileViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            holder = new LocalFileViewHolder();
            holder.nameText = convertView.findViewById(android.R.id.text1);
            holder.detailText = convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (LocalFileViewHolder) convertView.getTag();
        }
        
        File file = (File) items.get(position);
        holder.nameText.setText(file.getName());
        
        // Show file size and modified date
        long fileSize = file.length();
        String sizeText = android.text.format.Formatter.formatFileSize(context, fileSize);
        String modifiedText = DateUtils.getRelativeTimeSpanString(file.lastModified()).toString();
        holder.detailText.setText(sizeText + " • " + modifiedText);
        
        convertView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onLocalFileClick(file);
            }
        });
        
        convertView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onLocalFileLongClick(file);
            }
            return false;
        });
        
        return convertView;
    }
    
    private View createNetworkLinkView(int position, View convertView, ViewGroup parent) {
        NetworkLinkViewHolder holder;
        
        if (convertView == null) {
            // Create a custom layout for network links with thumbnail support
            convertView = createNetworkLinkLayout(parent);
            holder = new NetworkLinkViewHolder();
            holder.thumbnailImage = convertView.findViewById(R.id.thumbnail);
            holder.titleText = convertView.findViewById(R.id.title);
            holder.urlText = convertView.findViewById(R.id.url);
            holder.detailText = convertView.findViewById(R.id.details);
            holder.statusIcon = convertView.findViewById(R.id.status_icon);
            convertView.setTag(holder);
        } else {
            holder = (NetworkLinkViewHolder) convertView.getTag();
        }
        
        NetworkVideoLink link = (NetworkVideoLink) items.get(position);
        
        // Set title
        holder.titleText.setText(link.getDisplayTitle());
        
        // Set URL (truncated if too long)
        String url = link.getUrl();
        if (url.length() > 50) {
            url = url.substring(0, 47) + "...";
        }
        holder.urlText.setText(url);
        
        // Set details (format, duration, access count)
        StringBuilder details = new StringBuilder();
        if (!link.getFormat().isEmpty()) {
            details.append(link.getFormat());
        }
        if (link.getDuration() > 0) {
            if (details.length() > 0) details.append(" • ");
            details.append(link.getDurationString());
        }
        if (link.getAccessCount() > 0) {
            if (details.length() > 0) details.append(" • ");
            details.append("Played ").append(link.getAccessCount()).append(" times");
        }
        if (details.length() == 0) {
            details.append("Added ").append(DateUtils.getRelativeTimeSpanString(link.getDateAdded()));
        }
        holder.detailText.setText(details.toString());
        
        // Set thumbnail
        if (link.hasThumbnail()) {
            Bitmap thumbnail = BitmapFactory.decodeFile(link.getThumbnailPath());
            if (thumbnail != null) {
                holder.thumbnailImage.setImageBitmap(thumbnail);
            } else {
                holder.thumbnailImage.setImageResource(context.getResources().getIdentifier("ic_video_library_24dp", "drawable", context.getPackageName()));
            }
        } else {
            holder.thumbnailImage.setImageResource(context.getResources().getIdentifier("ic_video_library_24dp", "drawable", context.getPackageName()));
        }
        
        // Set status icon
        if (link.isValidUrl()) {
            holder.statusIcon.setImageResource(context.getResources().getIdentifier("ic_cloud_download_24dp", "drawable", context.getPackageName()));
            holder.statusIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else {
            holder.statusIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            holder.statusIcon.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        }
        
        convertView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onNetworkLinkClick(link);
            }
        });
        
        convertView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onNetworkLinkLongClick(link);
            }
            return false;
        });
        
        return convertView;
    }
    
    /**
     * Create a custom layout for network link items
     */
    private View createNetworkLinkLayout(ViewGroup parent) {
        // Create a horizontal LinearLayout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(16, 12, 16, 12);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        
        // Add thumbnail ImageView
        ImageView thumbnail = new ImageView(context);
        thumbnail.setId(R.id.thumbnail);
        android.widget.LinearLayout.LayoutParams thumbParams = new android.widget.LinearLayout.LayoutParams(
            (int) (48 * context.getResources().getDisplayMetrics().density),
            (int) (36 * context.getResources().getDisplayMetrics().density)
        );
        thumbParams.setMarginEnd(16);
        thumbnail.setLayoutParams(thumbParams);
        thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        layout.addView(thumbnail);
        
        // Add content LinearLayout (vertical)
        android.widget.LinearLayout contentLayout = new android.widget.LinearLayout(context);
        contentLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout.LayoutParams contentParams = new android.widget.LinearLayout.LayoutParams(
            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f
        );
        contentLayout.setLayoutParams(contentParams);
        
        // Add title TextView
        TextView title = new TextView(context);
        title.setId(R.id.title);
        title.setTextSize(16);
        title.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light));
        title.setMaxLines(1);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        contentLayout.addView(title);
        
        // Add URL TextView
        TextView url = new TextView(context);
        url.setId(R.id.url);
        url.setTextSize(12);
        url.setTextColor(ContextCompat.getColor(context, android.R.color.secondary_text_light));
        url.setMaxLines(1);
        url.setEllipsize(android.text.TextUtils.TruncateAt.END);
        contentLayout.addView(url);
        
        // Add details TextView
        TextView details = new TextView(context);
        details.setId(R.id.details);
        details.setTextSize(12);
        details.setTextColor(ContextCompat.getColor(context, android.R.color.tertiary_text_light));
        details.setMaxLines(1);
        details.setEllipsize(android.text.TextUtils.TruncateAt.END);
        contentLayout.addView(details);
        
        layout.addView(contentLayout);
        
        // Add status icon
        ImageView statusIcon = new ImageView(context);
        statusIcon.setId(R.id.status_icon);
        android.widget.LinearLayout.LayoutParams iconParams = new android.widget.LinearLayout.LayoutParams(
            (int) (24 * context.getResources().getDisplayMetrics().density),
            (int) (24 * context.getResources().getDisplayMetrics().density)
        );
        iconParams.setMarginStart(8);
        statusIcon.setLayoutParams(iconParams);
        layout.addView(statusIcon);
        
        return layout;
    }
    
    // ViewHolder classes
    private static class HeaderViewHolder {
        TextView titleText;
    }
    
    private static class LocalFileViewHolder {
        TextView nameText;
        TextView detailText;
    }
    
    private static class NetworkLinkViewHolder {
        ImageView thumbnailImage;
        TextView titleText;
        TextView urlText;
        TextView detailText;
        ImageView statusIcon;
    }
    
    // Custom IDs for views (to avoid conflicts)
    private static class R {
        private static class id {
            static final int thumbnail = 0x7F000001;
            static final int title = 0x7F000002;
            static final int url = 0x7F000003;
            static final int details = 0x7F000004;
            static final int status_icon = 0x7F000005;
        }
    }
}
