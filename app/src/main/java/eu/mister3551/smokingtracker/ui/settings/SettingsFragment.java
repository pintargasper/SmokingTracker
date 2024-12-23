package eu.mister3551.smokingtracker.ui.settings;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.mister3551.smokingtracker.MainActivity;
import eu.mister3551.smokingtracker.R;
import eu.mister3551.smokingtracker.Utils;
import eu.mister3551.smokingtracker.adapter.DropdownAdapter;
import eu.mister3551.smokingtracker.database.Manager;
import eu.mister3551.smokingtracker.database.download.Data;
import eu.mister3551.smokingtracker.database.interface_.Callback;
import eu.mister3551.smokingtracker.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private Manager manager;
    private Data data;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Button buttonSelectFile;
    private Uri uri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        this.manager = Utils.getManager().get();
        this.data = new Data(manager, this.getContext());

        TextView textViewVersion = root.findViewById(R.id.text_view_version);
        TextView textViewTermsOfService = root.findViewById(R.id.text_view_terms_of_service);
        TextView textViewPrivacyPolicy = root.findViewById(R.id.text_view_privacy_policy);
        TextView textViewAuthor = root.findViewById(R.id.text_view_author);
        Button buttonSelectLanguage = root.findViewById(R.id.button_select_language);
        Button buttonSelectColor = root.findViewById(R.id.button_select_color);
        Button buttonSelectPointColor = root.findViewById(R.id.button_select_point_color);
        Button buttonSelectLineColor = root.findViewById(R.id.button_select_line_color);
        Button buttonSelectPaintStyle = root.findViewById(R.id.button_select_paint_style);
        Button buttonSelectDownload= root.findViewById(R.id.button_select_download);
        Button buttonSelectUpload = root.findViewById(R.id.button_select_upload);

        buttonSelectLanguage.setOnClickListener(view -> showPopup(root.getContext(), "Language"));
        buttonSelectColor.setOnClickListener(view -> showPopup(root.getContext(), "Graph color"));
        buttonSelectPointColor.setOnClickListener(view -> showPopup(root.getContext(), "Point color"));
        buttonSelectLineColor.setOnClickListener(view -> showPopup(root.getContext(), "Line color"));
        buttonSelectPaintStyle.setOnClickListener(view -> showPopup(root.getContext(), "Paint style"));
        buttonSelectDownload.setOnClickListener(view -> showPopup(root.getContext(), "Download data"));
        buttonSelectUpload.setOnClickListener(view -> showPopup(root.getContext(), "Upload data"));

        String version = getAppVersion(root.getContext());
        Spannable spannable = (Spannable) Html.fromHtml(
                textViewVersion.getText().toString().replace("version", version)
                        .replace(version, "<a href='" + Utils.appVersionLink + "'>" + version + "</a>"),
                Html.FROM_HTML_MODE_LEGACY);

        textViewVersion.setText(spannable);
        textViewVersion.setMovementMethod(LinkMovementMethod.getInstance());
        textViewVersion.setText(spannable);
        textViewVersion.setMovementMethod(LinkMovementMethod.getInstance());

        textViewVersion.setMovementMethod(LinkMovementMethod.getInstance());
        textViewAuthor.setMovementMethod(LinkMovementMethod.getInstance());
        textViewTermsOfService.setMovementMethod(LinkMovementMethod.getInstance());
        textViewPrivacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        this.filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        uri = result.getData().getData();
                        if (uri != null) {
                            handleFile(uri, buttonSelectFile);
                        }
                    }
                });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showPopup(Context context, String title) {
        switch (title) {
            case "Language" -> showLanguagePopup(context);
            case "Graph color", "Line color", "Point color" -> showColorPopup(context, title);
            case "Paint style" -> showPaintStylePopup(context);
            case "Download data" -> showDownloadDataPopup(context);
            case "Upload data" -> showUploadDataPopup(context);
        }
    }

    private void showLanguagePopup(Context context) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.fragment_settings_popup_language, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Button buttonConfirm = bottomSheetView.findViewById(R.id.button_confirm);
        Button buttonClose = bottomSheetView.findViewById(R.id.button_close);
        Spinner spinnerLanguage = bottomSheetView.findViewById(R.id.spinner_language);

        CharSequence[] languages = context.getResources().getTextArray(R.array.language_array);

        DropdownAdapter adapter = new DropdownAdapter(context, R.layout.fragment_settings_dropdown, languages);
        adapter.setDropDownViewResource(R.layout.fragment_settings_dropdown);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setSelection(getSelectedLanguageIndex());

        buttonConfirm.setOnClickListener(view -> {
            Utils.getSettings().setLanguage(getLanguageCode(spinnerLanguage.getSelectedItemPosition()));
            MainActivity.setLanguage(context);
            requireActivity().recreate();
            manager.update(getLanguageCode(spinnerLanguage.getSelectedItemPosition()));
            bottomSheetDialog.dismiss();
        });

        buttonClose.setOnClickListener(view -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    private void showColorPopup(Context context, String title) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.fragment_settings_popup_colors, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView textViewSettingsMessage = bottomSheetView.findViewById(R.id.text_view_settings_message);

        String titleString = title.equals("Graph color") ? getString(R.string.text_view_graph_color) : title.equals("Point color") ? getString(R.string.text_view_point_color) : getString(R.string.text_view_line_color);
        textViewSettingsMessage.setText(titleString);

        Map<Integer, CheckBox> colorCheckBoxMap = initializeColorCheckBoxMap(bottomSheetView);

        setupColorCheckBoxListeners(title, colorCheckBoxMap);

        Button buttonConfirm = bottomSheetView.findViewById(R.id.button_confirm);
        buttonConfirm.setOnClickListener(view -> {
            applySelectedColors(title, colorCheckBoxMap);
            bottomSheetDialog.dismiss();
        });

        Button buttonClose = bottomSheetView.findViewById(R.id.button_close);
        buttonClose.setOnClickListener(view -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void showPaintStylePopup(Context context) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.fragment_settings_popup_paint_style, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Map<Paint.Style, CheckBox> paintStyleCheckBoxMap = initializePaintStyleCheckBoxMap(bottomSheetView);
        setupSingleSelectionListener(paintStyleCheckBoxMap, Utils.getSettings().getGraph().getPaintStyle());

        Button buttonConfirm = bottomSheetView.findViewById(R.id.button_confirm);
        Button buttonClose = bottomSheetView.findViewById(R.id.button_close);

        buttonConfirm.setOnClickListener(view -> {
            Paint.Style selectedStyle = getSelectedPaintStyle(paintStyleCheckBoxMap);
            Utils.getSettings().getGraph().setPaintStyle(selectedStyle);
            manager.update(selectedStyle);
            bottomSheetDialog.dismiss();
        });

        buttonClose.setOnClickListener(view -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    private void showDownloadDataPopup(Context context) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.fragment_settings_popup_download_data, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Spinner spinnerFormats = bottomSheetView.findViewById(R.id.spinner_selected_format);

        CharSequence[] formats = context.getResources().getTextArray(R.array.formats_array);

        DropdownAdapter adapter = new DropdownAdapter(context, R.layout.fragment_settings_dropdown, formats);
        adapter.setDropDownViewResource(R.layout.fragment_settings_dropdown);
        spinnerFormats.setAdapter(adapter);

        Button buttonDownload = bottomSheetView.findViewById(R.id.button_download);
        Button buttonClose = bottomSheetView.findViewById(R.id.button_close);

        buttonDownload.setOnClickListener(view -> {
            showLoadingDialog(context, spinnerFormats.getSelectedItem().toString().toLowerCase(), bottomSheetDialog, false);
        });

        buttonClose.setOnClickListener(view -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    private void showUploadDataPopup(Context context) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.fragment_settings_popup_upload_data, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        buttonSelectFile = bottomSheetView.findViewById(R.id.button_select_file);
        Button buttonUpload = bottomSheetView.findViewById(R.id.button_upload);
        Button buttonClose = bottomSheetView.findViewById(R.id.button_close);

        buttonSelectFile.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(intent);
        });

        buttonUpload.setOnClickListener(view -> {
            if (uri != null) {
                showLoadingDialog(context, getFileExtension(getFileName(uri)), bottomSheetDialog, true);
            }
        });

        buttonClose.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }

    private void showLoadingDialog(Context context, String selectedFormat, BottomSheetDialog bottomSheetDialog, boolean isUpload) {
        BottomSheetDialog loadingDialog = new BottomSheetDialog(context);
        View loadingView = getLayoutInflater().inflate(R.layout.fragment_loading_popup, null);
        bottomSheetDialog.dismiss();
        loadingDialog.setContentView(loadingView);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        new Thread(() -> {
            Callback callback = new Callback() {
                @Override
                public void onSuccess(Object object) {
                    handleDownloadSuccess(loadingView, object, loadingDialog);
                }

                @Override
                public void onError(String errorMessage) {
                    handleDownloadError(loadingView, errorMessage, loadingDialog);
                }
            };

            if (isUpload) {
                data.upload(uri, selectedFormat, callback);
            } else {
                data.download(selectedFormat, callback);
            }
        }).start();
    }

    private void handleDownloadSuccess(View loadingView, Object object, BottomSheetDialog loadingDialog) {
        new Handler(Looper.getMainLooper()).post(() -> {
            updateLoadingUI(loadingView, object.toString());
            Button buttonClose = loadingView.findViewById(R.id.button_close);
            buttonClose.setOnClickListener(view -> {
                loadingDialog.dismiss();
                Utils.getMainActivity().get().recreateActivity();
            });
        });
    }

    private void handleDownloadError(View loadingView, String errorMessage, BottomSheetDialog loadingDialog) {
        new Handler(Looper.getMainLooper()).post(() -> {
            updateLoadingUI(loadingView, errorMessage);
            Button buttonClose = loadingView.findViewById(R.id.button_close);
            buttonClose.setOnClickListener(view -> {
                loadingDialog.dismiss();
            });
        });
    }

    private void updateLoadingUI(View loadingView, String message) {
        LinearLayout linearLayoutOnLoading = loadingView.findViewById(R.id.linear_layout_on_loading);
        LinearLayout linearLayoutOnMessage = loadingView.findViewById(R.id.linear_layout_on_message);
        TextView textViewResponse = loadingView.findViewById(R.id.text_view_response);

        linearLayoutOnLoading.setVisibility(View.GONE);
        linearLayoutOnMessage.setVisibility(View.VISIBLE);
        textViewResponse.setText(message);
    }

    private Paint.Style getSelectedPaintStyle(Map<Paint.Style, CheckBox> paintStyleCheckBoxMap) {
        for (Map.Entry<Paint.Style, CheckBox> entry : paintStyleCheckBoxMap.entrySet()) {
            if (entry.getValue().isChecked()) {
                return entry.getKey();
            }
        }
        return Paint.Style.FILL;
    }

    private Map<Integer, CheckBox> initializeColorCheckBoxMap(View bottomSheetView) {
        Map<Integer, CheckBox> colorCheckBoxMap = new HashMap<>();
        colorCheckBoxMap.put(Color.GRAY, bottomSheetView.findViewById(R.id.check_box_grey));
        colorCheckBoxMap.put(Color.BLUE, bottomSheetView.findViewById(R.id.check_box_blue));
        colorCheckBoxMap.put(Color.RED, bottomSheetView.findViewById(R.id.check_box_red));
        colorCheckBoxMap.put(Color.YELLOW, bottomSheetView.findViewById(R.id.check_box_yellow));
        return colorCheckBoxMap;
    }

    private Map<Paint.Style, CheckBox> initializePaintStyleCheckBoxMap(View bottomSheetView) {
        Map<Paint.Style, CheckBox> colorCheckBoxMap = new HashMap<>();
        colorCheckBoxMap.put(Paint.Style.FILL, bottomSheetView.findViewById(R.id.check_box_fill));
        colorCheckBoxMap.put(Paint.Style.STROKE, bottomSheetView.findViewById(R.id.check_box_stroke));
        colorCheckBoxMap.put(Paint.Style.FILL_AND_STROKE, bottomSheetView.findViewById(R.id.check_box_fill_and_stroke));
        return colorCheckBoxMap;
    }

    private void setupColorCheckBoxListeners(String title, Map<Integer, CheckBox> colorCheckBoxMap) {
        switch (title) {
            case "Graph color" -> setupSingleSelectionListeners(colorCheckBoxMap, Utils.getSettings().getGraph().getColor());
            case "Point color" -> setupMultiSelectionListeners(colorCheckBoxMap, Utils.getSettings().getGraph().getPointColors());
            case "Line color" -> setupMultiSelectionListeners(colorCheckBoxMap, Utils.getSettings().getGraph().getLineColors());
            default -> throw new IllegalArgumentException("Unknown title: " + title);
        }
    }

    private void setupSingleSelectionListeners(Map<Integer, CheckBox> colorCheckBoxMap, int currentColor) {
        for (Map.Entry<Integer, CheckBox> entry : colorCheckBoxMap.entrySet()) {
            CheckBox checkBox = entry.getValue();
            int color = entry.getKey();
            checkBox.setChecked(color == currentColor);

            checkBox.setOnClickListener(view -> {
                if (checkBox.isChecked()) {
                    colorCheckBoxMap.values().forEach(otherCheckBox -> {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setChecked(false);
                        }
                    });
                } else if (colorCheckBoxMap.values().stream().noneMatch(CheckBox::isChecked)) {
                    Objects.requireNonNull(colorCheckBoxMap.get(Color.GRAY)).setChecked(true);
                }
            });
        }
    }

    private void setupSingleSelectionListener(Map<Paint.Style, CheckBox> colorCheckBoxMap, Paint.Style currentStyle) {
        for (Map.Entry<Paint.Style, CheckBox> entry : colorCheckBoxMap.entrySet()) {
            CheckBox checkBox = entry.getValue();
            Paint.Style paintStyle = entry.getKey();
            checkBox.setChecked(paintStyle == currentStyle);

            checkBox.setOnClickListener(view -> {
                if (checkBox.isChecked()) {
                    colorCheckBoxMap.values().forEach(otherCheckBox -> {
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.setChecked(false);
                        }
                    });
                } else if (colorCheckBoxMap.values().stream().noneMatch(CheckBox::isChecked)) {
                    Objects.requireNonNull(colorCheckBoxMap.get(Paint.Style.FILL)).setChecked(true);
                }
            });
        }
    }

    private void setupMultiSelectionListeners(Map<Integer, CheckBox> colorCheckBoxMap, List<Integer> currentColors) {
        for (Map.Entry<Integer, CheckBox> entry : colorCheckBoxMap.entrySet()) {
            CheckBox checkBox = entry.getValue();
            int color = entry.getKey();
            checkBox.setChecked(currentColors.contains(color));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) {
                    for (CheckBox otherCheckBox : colorCheckBoxMap.values()) {
                        if (otherCheckBox.isChecked()) {
                            return;
                        }
                    }
                    Objects.requireNonNull(colorCheckBoxMap.get(Color.GRAY)).setChecked(true);
                }
            });
        }
    }

    private void applySelectedColors(String title, Map<Integer, CheckBox> colorCheckBoxMap) {
        List<Integer> selectedColors = new ArrayList<>();
        for (Map.Entry<Integer, CheckBox> entry : colorCheckBoxMap.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedColors.add(entry.getKey());
            }
        }
        switch (title) {
            case "Graph color" -> Utils.getSettings().getGraph().setColor(selectedColors.get(0));
            case "Point color" -> Utils.getSettings().getGraph().setPointColors(selectedColors);
            case "Line color" -> Utils.getSettings().getGraph().setLineColors(selectedColors);
            default -> {
                return;
            }
        }
        manager.update(Utils.getSettings().getGraph().getColor(), convertPointColors(Utils.getSettings().getGraph().getPointColors()), convertPointColors(Utils.getSettings().getGraph().getLineColors()));
    }

    private int getSelectedLanguageIndex() {
        return switch (Utils.getSettings().getLanguage()) {
            case "Sl" -> 0;
            case "DE" -> 2;
            case "FR" -> 3;
            default -> 1;
        };
    }

    private String getLanguageCode(int index) {
        return switch (index) {
            case 0 -> "Sl";
            case 2 -> "DE";
            case 3 -> "FR";
            default -> "EN";
        };
    }

    private String convertPointColors(List<Integer> pointColors) {
        return pointColors.stream()
                .map(Object::toString)
                .collect(Collectors.joining(";"));
    }

    private void handleFile(Uri fileUri, Button button) {
        String fileName = getFileName(fileUri);
        if (fileName != null && !fileName.isEmpty()) {
            button.setText(fileName);
        } else {
            button.setText(requireContext().getString(R.string.str_unknown_file));
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        }
        return null;
    }

    public static String getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }
}