package com.example.stitching.Stitching;

import android.graphics.Bitmap;
import android.util.Pair;

import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.struct.DogArray;
import org.ddogleg.struct.FastAccess;

import java.util.ArrayList;
import java.util.List;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.PixelTransformHomography_F32;
import boofcv.alg.distort.impl.DistortSupport;
import boofcv.alg.interpolate.InterpolatePixelS;
import boofcv.android.ConvertBitmap;
import boofcv.factory.feature.associate.ConfigAssociateGreedy;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.border.BorderType;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.feature.TupleDesc_F64;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.Planar;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.transform.homography.HomographyPointOps_F64;

/*
 * Contains functions to compute homography over two images and their stitch.
 */

public class StitchingUtils {


    public static List<Point2D_F64> isTransformMessedUp(Homography2D_F64 T){
        // ab and cd are two parallel lines
        Point2D_F64 a = new Point2D_F64(0, 0);
        Point2D_F64 b = new Point2D_F64(150, 7);
        Point2D_F64 c = new Point2D_F64(20,0);
        Point2D_F64 d = new Point2D_F64(0, 0);


        // compute transformation on pointe
        Point2D_F64 at = new Point2D_F64();
        Point2D_F64 bt = new Point2D_F64();
//        Point2D_F64 ct = new Point2D_F64();
//        Point2D_F64 dt = new Point2D_F64();

        HomographyPointOps_F64.transform(T, a, at);
        HomographyPointOps_F64.transform(T, b, bt);
//        HomographyPointOps_F64.transform(T, c, ct);
//        HomographyPointOps_F64.transform(T, d, dt);

        // check if points flipped positions
        // initially xa < xc and ya > yc
        // initially xb > xd and yb > yd
        // true if one of the above flips

        ArrayList<Point2D_F64> result = new ArrayList<>();
        result.add(at);
        result.add(bt);
        return result;
//        if((at.x > ct.x && at.y < ct.y) || (bt.x < dt.x && bt.y <dt.y))
//            return true;
//        return false;
    }



    /**
     * Given two input images, compute the transform.
     */
    public static <T extends ImageGray<T>> Pair<Homography2D_F64, Double> stitch(T inputA, T inputB, Class<T> imageType) {
//        T inputA = ConvertBufferedImage.convertFromSingle(imageA, null, imageType);
//        T inputB = ConvertBufferedImage.convertFromSingle(imageB, null, imageType);

        // Detect using the standard SURF feature descriptor and describer
        DetectDescribePoint detDesc = FactoryDetectDescribe.surfStable(
                new ConfigFastHessian(1, 2, 200,
                        1, 9, 4, 4), null, null, imageType);

        ScoreAssociation<TupleDesc_F64> scorer = FactoryAssociation.scoreEuclidean(TupleDesc_F64.class, true);
        AssociateDescription<TupleDesc_F64> associate = FactoryAssociation.greedy(new ConfigAssociateGreedy(true, 2), scorer);




        // fit the images using a homography. This works well for rotations and distant objects.
        ModelMatcher<Homography2D_F64, AssociatedPair> modelMatcher = FactoryMultiViewRobust.homographyRansac(null, new ConfigRansac(60, 3));

        Homography2D_F64 H = computeTransform(inputA, inputB, detDesc, associate, modelMatcher);
        double fitScore = associate.getMatches().get(0).fitScore;

        return new Pair<>(H, fitScore);
    }

    /**
     *    Compute a transformation over imageA and ImageB
     *
     */
    public static <T extends ImageGray<T>, TD extends TupleDesc<TD>> Homography2D_F64 computeTransform(
            T imageA, T imageB, DetectDescribePoint<T, TD> detDesc, AssociateDescription<TD> associate,
            ModelMatcher<Homography2D_F64, AssociatedPair> modelMatcher) {
        // get the length of the description
        List<Point2D_F64> pointsA = new ArrayList<>();
        DogArray<TD> descA = UtilFeature.createArray(detDesc, 100);
        List<Point2D_F64> pointsB = new ArrayList<>();
        DogArray<TD> descB = UtilFeature.createArray(detDesc, 100);

        // extract feature locations and descriptions from each image
        describeImage(imageA, detDesc, pointsA, descA);
        describeImage(imageB, detDesc, pointsB, descB);

        // Associate features between the two images
        associate.setSource(descA);
        associate.setDestination(descB);
        associate.associate();

        // create a list of AssociatedPairs that tell the model matcher how a feature moved
        FastAccess<AssociatedIndex> matches = associate.getMatches();
        List<AssociatedPair> pairs = new ArrayList<>();

        for (int i = 0; i < matches.size(); i++) {
            AssociatedIndex match = matches.get(i);

            Point2D_F64 a = pointsA.get(match.src);
            Point2D_F64 b = pointsB.get(match.dst);

            pairs.add(new AssociatedPair(a, b, false));
        }

        // find the best fit model to describe the change between these images
        if (!modelMatcher.process(pairs)) throw new RuntimeException("Model Matcher failed!");

        // return the found image transform
        return modelMatcher.getModelParameters().copy();
    }
    /**
     * Detects features inside the two images and computes descriptions at those points.
     */
    private static <T extends ImageGray<T>, TD extends TupleDesc<TD>> void describeImage(
            T image, DetectDescribePoint<T, TD> detDesc, List<Point2D_F64> points, DogArray<TD> listDescs) {

        detDesc.detect(image);

        listDescs.reset();
        for (int i = 0; i < detDesc.getNumberOfFeatures(); i++) {
            points.add(detDesc.getLocation(i).copy());
            listDescs.grow().setTo(detDesc.getDescription(i));
        }
    }

    /**
     * compute the stitched image for vsualization and debug.
     */
    public static Bitmap computeStitchedImage(Planar<GrayF32> colorA, Planar<GrayF32> colorB,
                                              Homography2D_F64 fromAtoB) {
        // specify size of output image
        double scale = 0.5;

//        // Convert into a BoofCV color format
//        Planar<GrayF32> colorA =
//                ConvertBufferedImage.convertFromPlanar(imageA, null, true, GrayF32.class);
//        Planar<GrayF32> colorB =
//                ConvertBufferedImage.convertFromPlanar(imageB, null, true, GrayF32.class);

        // Where the output images are rendered into
        Planar<GrayF32> work = colorA.createSameShape();

        // Adjust the transform so that the whole image can appear inside of it
        Homography2D_F64 fromAToWork = new Homography2D_F64(scale, 0, colorA.width / 4, 0, scale, colorA.height / 4, 0, 0, 1);
        Homography2D_F64 fromWorkToA = fromAToWork.invert(null);

        // Used to render the results onto an image
        PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
        InterpolatePixelS<GrayF32> interp = FactoryInterpolation.bilinearPixelS(GrayF32.class, BorderType.ZERO);
        ImageDistort<Planar<GrayF32>, Planar<GrayF32>> distort =
                DistortSupport.createDistortPL(GrayF32.class, model, interp, false);
        distort.setRenderAll(false);

        // Render first image
        model.setTo(fromWorkToA);
        distort.apply(colorA, work);

        // Render second image
        Homography2D_F64 fromWorkToB = fromWorkToA.concat(fromAtoB, null);
        model.setTo(fromWorkToB);
        distort.apply(colorB, work);


//        // Convert the rendered image into a BufferedImage
//        BufferedImage output = new BufferedImage(work.width, work.height, imageA.getType());
//        ConvertBufferedImage.convertTo(work, output, true);
//

        Bitmap bitmap = Bitmap.createBitmap(work.width, work.height, Bitmap.Config.ARGB_8888);
        ConvertBitmap.boofToBitmap(work, bitmap, null);
        return bitmap;

//        Graphics2D g2 = output.createGraphics();
//
//        // draw lines around the distorted image to make it easier to see
//        Homography2D_F64 fromBtoWork = fromWorkToB.invert(null);
//        Point2D_I32 corners[] = new Point2D_I32[4];
//        corners[0] = renderPoint(0, 0, fromBtoWork);
//        corners[1] = renderPoint(colorB.width, 0, fromBtoWork);
//        corners[2] = renderPoint(colorB.width, colorB.height, fromBtoWork);
//        corners[3] = renderPoint(0, colorB.height, fromBtoWork);
//
//        g2.setColor(Color.ORANGE);
//        g2.setStroke(new BasicStroke(4));
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.drawLine(corners[0].x, corners[0].y, corners[1].x, corners[1].y);
//        g2.drawLine(corners[1].x, corners[1].y, corners[2].x, corners[2].y);
//        g2.drawLine(corners[2].x, corners[2].y, corners[3].x, corners[3].y);
//        g2.drawLine(corners[3].x, corners[3].y, corners[0].x, corners[0].y);
//
//        gui.setImage(0, 1, output);
    }

}
