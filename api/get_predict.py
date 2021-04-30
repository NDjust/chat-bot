from flask import Flask, render_template, redirect, url_for

# start flask
app = Flask(__name__)


def request_result(text):
    pass

# render default webpage
@app.route('/')
def home():
    return render_template('home.html')

# when the post method detect, then redirect to success function
@app.route('/', methods=['POST', 'GET'])
def get_data(request):
    if request.method == 'POST':
        user = request.form['search']
        return redirect(url_for('success', name=user))

# get the data for the requested query
@app.route('/success/<text>')
def success(text):
    return "<xmp>" + str(request_result(text)) + " </xmp> "