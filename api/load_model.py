import tensorflow as tf

sess = tf.Session()
# First let's load meta graph and restore weights
saver = tf.train.import_meta_graph(
    "/Users/hongnadan/PycharmProjects/capstone-chatbot/modeling/runs/1608212223/checkpoints/model-460.data-00000-of-00001")
saver.restore(sess, tf.train.latest_checkpoint(
    '/Users/hongnadan/PycharmProjects/capstone-chatbot/modeling/runs/1608212223/checkpoints/r'))
print(saver)

