package com.vaadin.recipes.recipe.camera;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.recipes.recipe.Metadata;
import com.vaadin.recipes.recipe.Recipe;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;

@Route("camera")
@Metadata(howdoI = "Take a photo from my phone", description = "You can use your phone camera with an HTML5 attribute")
public class CameraView extends Recipe {
  public CameraView() {
    MemoryBuffer buffer = new MemoryBuffer();
    Upload upload = new Upload(buffer);
    upload.setAcceptedFileTypes("image/*");
    // You can use the capture html5 attribute
    // https://caniuse.com/html-media-capture
    upload.getElement().setAttribute("capture", "environment");
    // If you don't compress the image, don't forget to increase the upload limit or you will have an error
    // For a spring boot application the default is 10MB, you can set it in application.properties:
    // spring.servlet.multipart.max-request-size=30MB
    Div output = new Div();

    upload.addSucceededListener(event -> {
      Component component = createComponent(event.getMIMEType(),
          event.getFileName(), buffer.getInputStream());
      showOutput(event.getFileName(), component, output);
    });

    add(upload, output);
  }

  private Component createComponent(String mimeType, String fileName,
                                    InputStream stream) {
    if (mimeType.startsWith("image")) {
      Image image = new Image();
      try {

        byte[] bytes = IOUtils.toByteArray(stream);
        image.getElement().setAttribute("src", new StreamResource(
            fileName, () -> new ByteArrayInputStream(bytes)));
        try (ImageInputStream in = ImageIO.createImageInputStream(
            new ByteArrayInputStream(bytes))) {
          final Iterator<ImageReader> readers = ImageIO
              .getImageReaders(in);
          if (readers.hasNext()) {
            ImageReader reader = readers.next();
            try {
              reader.setInput(in);
              image.setWidth(reader.getWidth(0) + "px");
              image.setHeight(reader.getHeight(0) + "px");
            } finally {
              reader.dispose();
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      return image;
    }
    Div content = new Div();
    String text = String.format("Mime type: '%s'\nSHA-256 hash: '%s'",
        mimeType, Arrays.toString(MessageDigestUtil.sha256(stream.toString())));
    content.setText(text);
    return content;

  }

  private void showOutput(String text, Component content,
                          HasComponents outputContainer) {
    HtmlComponent p = new HtmlComponent(Tag.P);
    p.getElement().setText(text);
    outputContainer.add(p);
    outputContainer.add(content);
  }
}