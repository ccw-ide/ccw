;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial implementation (code reviewed by Laurent Petit)
;*******************************************************************************/
(ns ^{:author "Andrea Richiardi"}
  ccw.editors.clojure.folding-support-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :as pp :refer [pprint]]
            [clojure.zip :as zip]
            [clojure.edn :as edn :refer [read-string]]
            [ccw.test-common :refer :all]
            [paredit.core :as p]
            [paredit.loc-utils :as lu]
            [ccw.editors.clojure.folding-support :refer :all]
            [ccw.editors.clojure.editor-support :as es])
  (:import org.eclipse.jface.text.Position
           ccw.editors.clojure.folding.FoldingDescriptor
           ccw.preferences.FoldingPreferencePage))

(def init-small-state #(es/init-text-buffer nil "(ns org.small.namespace)"))
(def init-medium-state #(es/init-text-buffer nil "(ns org.medium.namespace) (defn myfun \"Example\" [] (let [bind1 \"Ciao\" bind2 [\\b \\u \\d \\d \\y]] bind3 #{:a 1 :b 2 :c 3} (println bind1 bind2)))"))
(def init-empty-state #(es/init-text-buffer nil ""))

(def init-nested-list-state #(es/init-text-buffer nil "(filter even? (range 0 100))"))
(def init-single-line-state #(es/init-text-buffer nil "(ns ^{:doc \"test ns\"} ns.core)"))
(def init-multi-line-state #(es/init-text-buffer nil "(defn my-fun
 \"Lorem ipsum dolor sit amet,
 consectetur adipiscing elit,
 sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\"
 (println \"docet\"))
 "))

(with-private-vars [ccw.editors.clojure.folding-support [enabled?
                                                         enabled-tags
                                                         pos->vec
                                                         locs-with-tags
                                                         loc->position
                                                         inc-token-occ
                                                         dec-token-occ
                                                         update-token-map
                                                         son-of-a-multi-line-loc?
                                                         next-is-newline?
                                                         parse-locs
                                                         folding-positions
                                                         from-java
                                                         to-java]]
  (deftest folding-support-tests

    (testing "testing java interop ..."
      (let [descriptors [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}} {:id :fold-braces :enabled true :loc-tags #{:map :set}}]
            java-descriptors (map #(to-java FoldingDescriptor %) descriptors)
            tripped-descriptors (map #(from-java %) java-descriptors)]
        (is (= (:id descriptors) (:id tripped-descriptors)))
        (is (= (:enabled descriptors) (:enabled tripped-descriptors)))
        (is (= (:loc-tags descriptors) (:loc-tags tripped-descriptors))))
      (let [descriptors (edn/read-string FoldingPreferencePage/DEFAULT_FOLDING_DESCRIPTORS)
            java-descriptors (map #(to-java FoldingDescriptor %) descriptors)
            tripped-descriptors (map #(from-java %) java-descriptors)]
        (is (not (empty? java-descriptors)) "Converted descriptors should not be empty")
        (is (every? #(instance? FoldingDescriptor %) java-descriptors) "Converted descriptors should all be instances of FoldingDescriptor")
        (is (= descriptors tripped-descriptors) "Round trip from/to java for the default preference descriptors")))

    (testing "testing update-token-map and inc/dec-token-occ..."
      (let [parse-tree (:parse-tree @(init-medium-state))
            root-loc (lu/parsed-root-loc parse-tree)
            loc-seq (locs-with-tags root-loc #{:list :vector})
            list-loc1 (first (take-while #(= :list (lu/loc-tag %1)) loc-seq))
            list-loc2 (second (take-while #(= :list (lu/loc-tag %1)) loc-seq))
            vector-loc (take-while #(= :vector (lu/loc-tag %1)) loc-seq)
            initial-map (update-token-map {} "(" list-loc1)
            list-vector-map (update-token-map initial-map "[" vector-loc)
            list2-vector-map (update-token-map list-vector-map "(" list-loc2)]
        (is (= {"(" {:occurrences 1 :loc list-loc1}} initial-map) "Initialization should conj to map")
        (is (= {"(" {:occurrences 1 :loc list-loc1}, "[" {:occurrences 1 :loc vector-loc}}
               list-vector-map) "Updating a map with different token should add an entry")
        (is (= {"(" {:occurrences 2 :loc list-loc1}, "[" {:occurrences 1 :loc vector-loc}}
               list2-vector-map) "Updating a map with a token already preset should increase :occurrences")
        (is (not (= {"(" {:occurrences 2 :loc list-loc2}, "[" {:occurrences 1 :loc vector-loc}}
                    list2-vector-map)) "Updating a map with a token already set should not changing the initial loc")
        (is (= {"(" {:occurrences 2 :loc list-loc1}, "[" {:occurrences 1 :loc vector-loc}}
               (inc-token-occ list-vector-map "(")) "Should correctly increment (")
        (is (= {"(" {:occurrences 1 :loc list-loc1}, "[" {:occurrences 0 :loc vector-loc}}
               (dec-token-occ list-vector-map "[")) "Should correctly decrement [")))

    (testing "testing locs-with-tags..."
      (let [parse-tree (:parse-tree @(init-medium-state))
            root-loc (lu/parsed-root-loc parse-tree)]
        (is (every? #(= (lu/loc-tag %1) :vector) (locs-with-tags root-loc #{:vector})))
        (is (every? #(= (lu/loc-tag %1) :list) (locs-with-tags root-loc #{:list})))
        (is (every? #(get #{:vector :string-body} (lu/loc-tag %1)) (locs-with-tags root-loc #{:vector :string-body})))
        (is (nil? (locs-with-tags root-loc #{:not-there})) "Should return a nil if empty")))

    (testing "testing son-of-a-multi-line-loc? ..."
      (let [parse-tree (:parse-tree @(init-single-line-state))
            root-loc (lu/parsed-root-loc parse-tree)
            loc-seq (locs-with-tags root-loc #{:list :string})]
        ;; Refer to the samples above to visually see the test case(s)
        ;; single line loc should stay, as the word says, on a unique line with no "\n"
        (is (= [false false false false] (map son-of-a-multi-line-loc? loc-seq))) "When locs are on a single line, it should always return true")
      (let [parse-tree (:parse-tree @(init-multi-line-state))
            root-loc (lu/parsed-root-loc parse-tree)
            loc-seq (locs-with-tags root-loc #{:list :string})]
        ;; Refer to the samples above to visually see the test case(s)
        (is (= [true true true false false false false true] (map son-of-a-multi-line-loc? loc-seq))) "Should correctly report when loc enclose in a multi-line form"))

    ;; (testing "testing enclose?"
    ;; (is (enclose? (Position. 2 40) (Position. 3 30)) "At non boundaries enclose? returns true [1]")
    ;; (is (not (enclose? (Position. 2 40) (Position. 2 30))) "At boundaries enclose? returns false [1]")
    ;; (is (enclose? (Position. 4 10) (Position. 5 8)) "At non boundaries enclose? returns true [2]")
    ;; (is (not (enclose? (Position. 4 10) (Position. 5 9))) "At boundaries enclose? returns false [2]")
    ;; )

    ;; (testing "testing overlap?"
    ;; (is (not (overlap? [])) "false on empty seqs")
    ;; (is (not (overlap? [(Position. 2 40)])) "false on one-element seqs")
    ;; (is (not (overlap? [(Position. 1 3) (Position. 4 10) (Position. 15 8)])) "Positions should not overlap")
    ;; (is (not (overlap? [(Position. 2 40) (Position. 42 10)])) "Pos1's offset+length equal to pos2's offset does not overlap")
    ;; (is (overlap? [(Position. 2 40) (Position. 41 10)]) "Pos1's offset+length less than pos2's offset does overlap")
    ;; (is (overlap? [(Position. 2 40) (Position. 1 30) (Position. 4 10) (Position. 5 8)]) "Every position should not overlap with each other [second is wrong]"))

    (testing "testing next-is-newline? ..."
      (let [parse-tree (:parse-tree @(init-multi-line-state))
            root-loc (lu/parsed-root-loc parse-tree)
            loc-seq (locs-with-tags root-loc #{:list :string})]
        ;; Refer to the samples above to visually see the test case(s)
        (is (= [false false true false false false false true] (map next-is-newline? loc-seq ))) "Should correctly report when loc is newline"))

    (testing "testing parse-locs..."
      (let [parse-tree (:parse-tree @(init-small-state))
            root-loc (lu/parsed-root-loc parse-tree)
            loc-seq (locs-with-tags root-loc #{:list})
            position-set (trampoline parse-locs loc-seq {} #{})]
        (is (set? position-set) "A set of org.eclipse.jface.text.Position should be returned")
        (is (= [[1 22]] (map pos->vec position-set))) "Folding at the right place")
      (let [parse-tree (:parse-tree @(init-nested-list-state))
            root-loc (lu/parsed-root-loc parse-tree)
            loc-seq (locs-with-tags root-loc #{:list})]
        (is (= [[1 26]] (map pos->vec (trampoline parse-locs loc-seq {} #{})))) "Only top-level list is folded"))

    (testing "testing enabled? ..."
      (let [all-disabled-descriptors [{:id :fold-parens :enabled false :loc-tags #{:list}} {:id :fold-double-apices :enabled false :loc-tags #{:string}}]
            first-disabled-descriptors [{:id :fold-parens :enabled false :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]
            second-disabled-descriptors [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled false :loc-tags #{:string}}]
            all-enabled-descriptors [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]]
        (is (not (enabled? all-disabled-descriptors)) "If all descriptors are disabled return false")
        (is (enabled? first-disabled-descriptors) "If second descriptor is enabled return true")
        (is (enabled? second-disabled-descriptors) "If first descriptor is enabled return true")
        (is (enabled? all-enabled-descriptors) "If all descriptors are enabled return true")))

    (testing "testing enabled-tags ..."
      (let [all-disabled-descs [{:id :fold-parens :enabled false :loc-tags #{:list}} {:id :fold-double-apices :enabled false :loc-tags #{:string}}]
            string-enabled-descs [{:id :fold-parens :enabled false :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]
            list-enabled-descs [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled false :loc-tags #{:string}}]
            all-enabled-descs [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]
            multiple-tags-descs [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}} {:id :fold-braces :enabled true :loc-tags #{:map :set}}]]
        (is (= #{} (enabled-tags all-disabled-descs)) "If all descriptors are disabled return empty set")
        (is (= #{:string} (enabled-tags string-enabled-descs)) "If string descriptor is enabled return #{:string}")
        (is (= #{:list} (enabled-tags list-enabled-descs)) "If list descriptor is enabled return #{:list}")
        (is (= #{:list :string} (enabled-tags all-enabled-descs)) "If all enabled return #{:list :string}")
        (is (= #{:list :string :map :set} (enabled-tags multiple-tags-descs)) "With multiple tags, all enabled, return #{:list :string :map :set}")))

    (testing "testing folding-positions..."
      (let [descriptors [{:id :fold-parens :enabled false :loc-tags #{:list}} {:id :fold-double-apices :enabled false :loc-tags #{:string}}]
            position-set (folding-positions @(init-small-state) descriptors)]
        (is (empty? position-set)) "if no descriptor is enabled folding should be empty")
      (let [descriptors [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]
            position-set (folding-positions @(init-small-state) descriptors)]
        (is (set? position-set) "A set of org.eclipse.jface.text.Position should be returned")
        (is (empty? position-set)) "Small state folding should be empty")
      (let [descriptors [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]
            position-set (folding-positions @(init-single-line-state) descriptors)]
        (is (empty? position-set)) "Single-line state folding should be empty")
      (let [all-descriptors [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]
            list-descriptors [{:id :fold-parens :enabled true :loc-tags #{:list}} {:id :fold-double-apices :enabled false :loc-tags #{:string}}]
            string-descriptors [{:id :fold-parens :enabled false :loc-tags #{:list}} {:id :fold-double-apices :enabled true :loc-tags #{:string}}]]
        (is (some #{[1 159] [15 125]} (map pos->vec (folding-positions @(init-multi-line-state) all-descriptors))) "Multi-line state (:list and :string enabled) should return only two positions")
        (is (some #{[1 159]} (map pos->vec (folding-positions @(init-multi-line-state) list-descriptors))) "Multi-line state (:list enabled) should return only the first position")
        (is (some #{[15 125]} (map pos->vec (folding-positions @(init-multi-line-state) string-descriptors))) "Multi-line state (:string enabled) should return only the second position")))))

;; (run-tests)
