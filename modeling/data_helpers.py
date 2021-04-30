import numpy as np
import pickle


def load_pickle_data(filename):
    with open(filename, "rb") as f:
        data = pickle.load(f)

    return data


def load_data_and_labels(plain_data_file, toxic_data_file):
    """ Load plain & toxic data

    :param plain_data_file: 욕설이 없는 문장파일
    :param toxic_data_file: 욕설이 있는 문장파일
    :return: 라벨링된 데이터
    """
    # Load data from files
    positive_examples = load_pickle_data(plain_data_file)
    positive_examples = [s.strip() for s in positive_examples]

    negative_examples = load_pickle_data(toxic_data_file)
    negative_examples = [s.strip() for s in negative_examples]

    # Split by words
    x_text = positive_examples + negative_examples
    x_text = [sent for sent in x_text]
    # Generate labels
    positive_labels = [[0, 1] for _ in positive_examples]
    negative_labels = [[1, 0] for _ in negative_examples]
    y = np.concatenate([positive_labels, negative_labels], 0)
    return [x_text, y]


def batch_iter(data, batch_size, num_epochs, shuffle=True):
    # Generates a batch iterator for a dataset.
    data = np.array(data)
    data_size = len(data)
    num_batches_per_epoch = int((len(data)-1)/batch_size) + 1
    for epoch in range(num_epochs):
        # Shuffle the data at each epoch
        if shuffle:
            shuffle_indices = np.random.permutation(np.arange(data_size))
            shuffled_data = data[shuffle_indices]
        else:
            shuffled_data = data
        for batch_num in range(num_batches_per_epoch):
            start_index = batch_num * batch_size
            end_index = min((batch_num + 1) * batch_size, data_size)
            yield shuffled_data[start_index:end_index]