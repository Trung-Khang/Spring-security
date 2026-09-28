package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;
    private final boolean configured;

    public CloudinaryServiceImpl(
            Cloudinary cloudinary,
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret) {
        this.cloudinary = cloudinary;
        this.configured = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        requireConfiguration();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh cần tải lên.");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return new CloudinaryUploadResult(
                    result.get("secure_url").toString(), result.get("public_id").toString());
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Không thể tải ảnh lên Cloudinary.", exception);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        requireConfiguration();
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Không thể xóa ảnh khỏi Cloudinary.", exception);
        }
    }

    private void requireConfiguration() {
        if (!configured) {
            throw new IllegalStateException("Hãy cấu hình Cloudinary trong file .env trước khi tải ảnh.");
        }
    }
}
