package com.topcoder.shared.problem;

/**
 * Interface for all renderer objects.
 * @author Greg Paul
 */
public interface ElementRenderer extends Renderer {


    /**
     * Set the element for this renderer.
     * @param element the element
     * @throws Exception if the renderer is not capable of rendering the given element
     */
    void setElement(Element element) throws Exception;
}

