import torch


class LSTMClassifier1(torch.nn.Module):

    def __init__(self, embedding_dim, hidden_dim, vocab_size, tagset_size, num_lstm_layers, is_bidirectional):
        super(LSTMClassifier1, self).__init__()
        #
        self.embedding_dim = embedding_dim
        self.hidden_dim = hidden_dim
        self.vocab_size = vocab_size
        self.tagset_size = tagset_size
        self.num_lstm_layers = num_lstm_layers
        self.is_bidirectional = is_bidirectional
        #
        self.word_embeddings = torch.nn.Embedding(vocab_size, embedding_dim) if embedding_dim > 1 else None
        self.lstm1 = torch.nn.LSTM(embedding_dim, hidden_dim, num_layers=num_lstm_layers, bidirectional=is_bidirectional)
        self.fc1 = torch.nn.Linear(hidden_dim * 2 if is_bidirectional else hidden_dim, tagset_size)

    def forward(self, seq):
        n = len(seq)
        out = self.word_embeddings(seq) if self.word_embeddings is not None else seq.float()
        out = out.view(n, 1, -1)
        out, _ = self.lstm1(out)
        if self.is_bidirectional:
            out = out[:, -1, :]
        else:
            out = out.view(n, -1)
        out = self.fc1(out)
        if not self.training:
            out = torch.nn.functional.log_softmax(out, dim=1)
        return out


class GRUClassifier1(torch.nn.Module):

    def __init__(self, embedding_dim, hidden_dim, vocab_size, tagset_size, num_gru_layers, is_bidirectional):
        super(GRUClassifier1, self).__init__()
        #
        self.embedding_dim = embedding_dim
        self.hidden_dim = hidden_dim
        self.vocab_size = vocab_size
        self.tagset_size = tagset_size
        self.num_gru_layers = num_gru_layers
        self.is_bidirectional = is_bidirectional
        #
        self.word_embeddings = torch.nn.Embedding(vocab_size, embedding_dim) if embedding_dim > 1 else None
        self.gru1 = torch.nn.GRU(embedding_dim, hidden_dim, num_layers=num_gru_layers, bidirectional=is_bidirectional)
        self.fc1 = torch.nn.Linear(hidden_dim * 2 if is_bidirectional else hidden_dim, tagset_size)

    def forward(self, seq):
        n = len(seq)
        out = self.word_embeddings(seq) if self.word_embeddings is not None else seq.float()
        out = out.view(n, 1, -1)
        out, _ = self.gru1(out)
        if self.is_bidirectional:
            out = out[:, -1, :]
        else:
            out = out.view(n, -1)
        out = self.fc1(out)
        if not self.training:
            out = torch.nn.functional.log_softmax(out, dim=1)
        return out


class RNNClassifier1(torch.nn.Module):

    def __init__(self, embedding_dim, hidden_dim, vocab_size, tagset_size, num_layers, is_bidirectional):
        super().__init__()
        self.embedding_dim = embedding_dim
        self.hidden_dim = hidden_dim
        self.vocab_size = vocab_size
        self.tagset_size = tagset_size
        self.num_layers = num_layers
        self.is_bidirectional = is_bidirectional
        #
        self.word_embeddings = torch.nn.Embedding(vocab_size, embedding_dim) if embedding_dim > 1 else None
        self.rnn = torch.nn.RNN(embedding_dim, hidden_dim, num_layers=num_layers, bidirectional=is_bidirectional)
        self.fc1 = torch.nn.Linear(hidden_dim * 2 if is_bidirectional else hidden_dim, tagset_size)


    def forward(self, seq):
        n = len(seq)
        out = self.word_embeddings(seq) if self.word_embeddings is not None else seq.float()
        out = out.view(n, 1, -1)
        out, _ = self.rnn(out)
        if self.is_bidirectional:
            out = out[:, -1, :]
        else:
            out = out.view(n, -1)
        out = self.fc1(out)
        if not self.training:
            out = torch.nn.functional.log_softmax(out, dim=1)
        return out
