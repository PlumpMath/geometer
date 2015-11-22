(ns geometer.app
  (:require [thi.ng.typedarrays.core :as arrays]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.core.vector :as v :refer [vec2 vec3]]
            [thi.ng.geom.core.matrix :as mat :refer [M44]]
            [thi.ng.geom.core.utils :as gu]
            [thi.ng.geom.webgl.core :as gl]
            [thi.ng.geom.webgl.animator :as anim]
            [thi.ng.geom.webgl.buffers :as buf]
            [thi.ng.geom.webgl.shaders :as sh]
            [thi.ng.geom.webgl.animator :refer [animate]]
            [thi.ng.geom.webgl.shaders.basic :as basic]))

(defonce mouse-x (atom 0))
(defonce mouse-y (atom 0))

(defonce gl (gl/gl-context "main"))
(defonce view-rect (gl/get-viewport-rect gl))

(def model
  (let [vertices (map #(* % (+ 0.3 (* 0.3 (rand)))) [1 -1 1 1 -1 -1 1 1 -1 1 -1 1 1 1 -1 1 1 1 -1 -1 -1 -1 -1 1 -1 1 1 -1 -1 -1 -1 1 1 -1 1 -1 -1 1 -1 -1 1 1 1 1 1 -1 1 -1 1 1 1 1 1 -1 -1 -1 -1 1 -1 -1 1 -1 1 -1 -1 -1 1 -1 1 -1 -1 1 -1 -1 1 1 -1 1 1 1 1 -1 -1 1 1 1 1 -1 1 1 -1 -1 -1 -1 1 -1 1 1 -1 -1 -1 -1 1 1 -1 1 -1 -1])
        colors   (flatten (map (partial repeat 6)
                               [[0.9 0.3 0.3 1]
                                [0.3 0.9 0.3 1]
                                [0.3 0.3 0.9 1]
                                [0.9 0.9 0.3 1]
                                [0.9 0.3 0.9 1]
                                [0.0 0.9 0.9 1]]))]
    (-> {:attribs {:position {:data (arrays/float32 vertices) :size 3}
                   :color    {:data (arrays/float32 colors) :size 4}}
         :uniforms     {:proj (gl/perspective 45 view-rect 0.1 100.0)
                        :view (mat/look-at (v/vec3 0 0 2) (v/vec3) v/V3Y)}
         :mode         gl/triangles
         :num-vertices (/ (count vertices) 3)
         :shader       (->> (basic/make-shader-spec-3d true)
                            (sh/make-shader-from-spec gl))}
                      (buf/make-attribute-buffers-in-spec gl gl/static-draw))))

(defn ^:export start []
  (.addEventListener
   js/document "mousemove"
   #(do
      (reset! mouse-x (* 0.01 (- (.-clientX %) (/ (.-innerWidth js/window) 2))))
      (reset! mouse-y (* 0.01 (- (.-clientY %) (/ (.-innerHeight js/window) 2))))))

  (anim/animate
   (fn [[t frame]]
     (gl/set-viewport gl view-rect)
     (gl/clear-color-buffer gl 1 1 1 1)
     (gl/enable gl gl/depth-test)
     (buf/draw-arrays-with-shader gl (assoc-in model [:uniforms :model]
                                               (-> M44
                                                   (g/rotate-x @mouse-y)
                                                   (g/rotate-y @mouse-x))))
     true)))
