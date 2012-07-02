(ns maze.core)

;; Let C be a list with one random cell in it.
;; Choose 1 cell from C and one neighbout not from C
;; if not possible - remove cell from C
;; else draw path between cells
;; repeat until c is empty

(defn top-neighbour [c]
  (when (> c 9)
    (- c 10)))

(defn bottom-neighbour [c]
  (when (< c 90)
    (+ c 10)))

(defn right-neighbour [c]
  (when (not (= 9 (rem c 10)))
    (inc c)))

(defn left-neighbour [c]
  (when (not (zero? (rem c 10)))
    (dec c)))

(defn neighbours [c]
  (filter identity [(top-neighbour c) (bottom-neighbour c) (right-neighbour c) (left-neighbour c)]))

(defn choose-from-c [c]
  (rand-nth (seq c)))

(defn choose-neighbour-from-candidates [cands]
  (when (seq cands)
    (rand-nth cands)))

(defn find-neighbour-not-in-c [cell c complete]
  (choose-neighbour-from-candidates (remove (into c complete) (neighbours cell))))

(defn make-path [grid cell-a cell-b]
  (conj grid [cell-a cell-b]))

(defn add-to-c [c cell]
  (conj c cell))

(defn remove-from-c [c cell]
  (disj c cell))

(defn maze-gen
  "The growing tree algorithm for maze generation."
  [grid c complete]
  (if (empty? c)
    grid
    (let [cell-from-c (choose-from-c c)
          neighbour  (find-neighbour-not-in-c cell-from-c c complete)]
      (if neighbour
        (recur (make-path grid cell-from-c neighbour) (add-to-c c neighbour) complete)
        (recur grid (remove-from-c c cell-from-c) (conj complete cell-from-c))))))

(defn path? [grid cell mv-fn]
  (or (contains? grid [cell (mv-fn cell)])
      (contains? grid [(mv-fn cell) cell])))

(defn n-fn [c] (- c 10))
(def e-fn inc)
(defn s-fn [c] (+ c 10))
(def w-fn dec)

(defn n-path? [grid cell] (path? grid cell n-fn))
(defn e-path? [grid cell] (path? grid cell e-fn))
(defn s-path? [grid cell] (path? grid cell s-fn))
(defn w-path? [grid cell] (path? grid cell w-fn))

(defn draw-maze [grid]
  (println "*-*-*-*-*-*-*-*-*-*-*")
  (doseq [[cell horizontal] (mapcat (fn [r] (concat (map #(vector % true) r) (map #(vector % false) r))) (partition 10 (range 0 100)))]
    (if (zero? (rem cell 10))
      (print (if horizontal "|" "*")))
    (if horizontal
      (print (if (h-path? grid cell) "  " " |"))
      (print (if (v-path? grid cell) " *" "-*")))
    (if (= 9 (rem cell 10))
      (println))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn view [pos grid path-pred mv-fn]
  (loop [length 0 cell pos]
    (if (path-pred grid cell)
      (recur (inc length) (mv-fn cell))
      length)))

(defn n-view [pos grid] (view pos grid n-path? n-fn))
(defn e-view [pos grid] (view pos grid e-path? e-fn))
(defn s-view [pos grid] (view pos grid s-path? s-fn))
(defn w-view [pos grid] (view pos grid w-path? w-fn))

(def mv-fns {'n n-fn
             'e e-fn
             's s-fn
             'w w-fn})

(def path-preds {'n n-path?
                 'e e-path?
                 's s-path?
                 'w w-path?})

(defn apply-move [cell move grid]
  ;; check for valid move
  ((mv-fns move) cell))

(defn append-to-path [path move grid]
  (conj path move))

(defn play-maze [grid maze-fn start tries path]
  (cond
    (zero? tries) ['failure path]
    (= 99 start) ['success path]
    :else (let [move (maze-fn (n-view start grid)
                              (e-view start grid)
                              (s-view start grid)
                              (w-view start grid)
                              tries
                              path)]
            (recur grid
                   maze-fn
                   (apply-move start move grid)
                   (dec tries)
                   (append-to-path path move grid)))))

