package bel.learn._16_googleDiff;

import bel.learn._30_webBrowser.JavaWebBrowser;
import bel.util.DiffMatchPatch;

import java.util.LinkedList;
import java.util.List;

/**
 * Created 03.04.2017.
 */
public class GoogleDiffLearnMain {
    static String s1 = "Das ist ein Text\n"
            + "mit Zeilenumbrüchen und darüber hinaus\n"
            + "mit Satzzeichen, seltsömen Worten, Versorten\n"
            + "und einem Punkt am Ende.";


    static String s3 = "Das ist ein Text\n"
            + "mit Zeilenumbrüchen oder was und darüber hinaus\n"
            + "mit Satzzeichen, Worten, Versorten\n"
            + "und einem Punkt am Ende. Und noch wös.";

    public static void main(String[] args) throws InterruptedException {
        diffToStringAndPatch();
        diffOnPlainText();
        diffOnHtml();
    }

    private static void diffToStringAndPatch() {

        LinkedList<DiffMatchPatch.Diff> diffs = DiffMatchPatch.get().diff_main(s3, s1);

        // 3 types of making patches... different?
        LinkedList<DiffMatchPatch.Patch> patches = DiffMatchPatch.get().patch_make(diffs);
        LinkedList<DiffMatchPatch.Patch> patches1 = DiffMatchPatch.get().patch_make(s3, s1);
        LinkedList<DiffMatchPatch.Patch> patches2 = DiffMatchPatch.get().patch_make(s3, diffs);

        // a patch can be turned to text
        String patchTextLine = DiffMatchPatch.get().patch_toText(patches);
        System.out.println("PATCH: \n" + patchTextLine);
        List<DiffMatchPatch.Patch> patchesShouldMakeS1FromS3 = DiffMatchPatch.get().patch_fromText(patchTextLine);

        LinkedList<DiffMatchPatch.Patch> patchLinkedList = new LinkedList<>(patchesShouldMakeS1FromS3);
        // Test...
        Object[] recoveredS1 = DiffMatchPatch.get().patch_apply(patchLinkedList, s3);
        assert recoveredS1[0].equals(s1);


        // then there is Delta. whats difference to patch? Mainly, that it is stored in one line
        String delta = DiffMatchPatch.get().diff_toDelta(diffs);
        System.out.println("DELTA: \n" + delta);
        LinkedList<DiffMatchPatch.Diff> diffs1 = DiffMatchPatch.get().diff_fromDelta(s3, delta);
        String somethingLikeS1 = DiffMatchPatch.get().diff_text2(diffs1);
        assert somethingLikeS1.equals(s1);


    }

    private static void diffOnHtml() {

    }

    private static void diffOnPlainText() {
        DiffMatchPatch dmp = DiffMatchPatch.get();
        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(s1, s3);
        dmp.diff_cleanupSemantic(diffs);

        LinkedList<DiffMatchPatch.Patch> patches = dmp.patch_make(diffs);
        String patch = dmp.patch_toText(patches);
        System.out.println("Patch = " + patch);

        String delta = dmp.diff_toDelta(diffs);
        System.out.println("Delta = " + delta);


        String htmlDiff = dmp.diff_prettyHtml(diffs);
        JavaWebBrowser wb = new JavaWebBrowser();
        wb.startInJFrame();
        //Thread.sleep(500);
        //System.out.println(htmlDiff);
        wb.loadContent(htmlDiff);
    }
}
