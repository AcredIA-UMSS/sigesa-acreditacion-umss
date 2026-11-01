package com.umss.sigesa.application.port.out;

import java.util.List;

public interface DraftingDocumentRendererPort {

    byte[] render(String title, List<String> paragraphs);
}
